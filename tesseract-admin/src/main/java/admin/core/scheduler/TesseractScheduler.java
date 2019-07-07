package admin.core.scheduler;

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

@Slf4j
public class TesseractScheduler implements InitializingBean, DisposableBean {
    @Autowired
    private ITesseractTriggerService tesseractTriggerService;

    @Autowired
    private TesseractTriggerDispatcher tesseractTriggerDispatcher;


    private int maxBatchSize = 50;
    private int timeWindowSize = 10;
    private int sleepTime = 20;

    private Thread schedulerThread;
    private volatile boolean isStop = false;
    private volatile boolean isPause = true;

    public void destroy() {
        isStop = true;
        schedulerThread.interrupt();
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
                List<TesseractTrigger> triggerList = tesseractTriggerService.findTriggerWithLock(maxBatchSize, System.currentTimeMillis(), timeWindowSize);
                log.info("schedulerThread扫描到触发器：{}", triggerList);
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
                try {
                    Thread.sleep(sleepTime * 1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }


}
