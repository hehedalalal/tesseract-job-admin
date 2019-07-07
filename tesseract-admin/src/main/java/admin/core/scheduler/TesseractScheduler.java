package admin.core.scheduler;

import admin.core.scheduler.pool.DefaultSchedulerThreadPool;
import admin.core.scheduler.pool.ISchedulerThreadPool;
import admin.entity.TesseractTrigger;
import admin.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TesseractScheduler implements InitializingBean, DisposableBean {
    @Autowired
    private ITesseractTriggerService tesseractTriggerService;

    @Autowired
    private TesseractTriggerDispatcher tesseractTriggerDispatcher;


    private int maxBatchSize = 250;
    private int timeWindowSize = 0;
    private int sleepTime = 20;

    private Thread schedulerThread;
    private volatile boolean isStop = false;
    private volatile boolean isPause = true;
    private volatile AtomicInteger atomicInteger = new AtomicInteger(0);

    public void destroy() {
        isStop = true;
        schedulerThread.interrupt();
        //停止dispatcher
        tesseractTriggerDispatcher.stop();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        isPause = false;
        log.info("调度器启动");
    }

    @Override
    public void afterPropertiesSet() {
        //开启线程
        schedulerThread = new Thread(new TriggerTaskRunnable(), "SchedulerThread");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
        log.info("schedulerThread start");
    }

    private class TriggerTaskRunnable implements Runnable {

        @Override
        public void run() {
            while (!isStop) {
                while (!isStop && isPause) {
                    try {
                        //响应中断
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                }
                int blockGetAvailableThreadNum = tesseractTriggerDispatcher.blockGetAvailableThreadNum();
                log.info("可用线程数:{}", blockGetAvailableThreadNum);
                List<TesseractTrigger> triggerList = tesseractTriggerService.findTriggerWithLock(blockGetAvailableThreadNum, System.currentTimeMillis(), timeWindowSize);
                log.info("扫描触发器数量:{}", triggerList.size());
                if (!CollectionUtils.isEmpty(triggerList)) {
                    //降序排序等待时间差
                    TesseractTrigger tesseractTrigger = triggerList.get(0);
                    Long nextTriggerTime = tesseractTrigger.getNextTriggerTime();
                    long time = System.currentTimeMillis() - nextTriggerTime;
                    //5s时间差
                    if (time > 5) {
                        synchronized (this) {
                            try {
                                this.wait(time - 5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    tesseractTriggerDispatcher.dispatchTrigger(triggerList, false);
                    continue;
                }
                log.error(atomicInteger.toString());
                try {
                    Thread.sleep(sleepTime * 1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }


}
