package admin.core.scheduler;

import admin.entity.TesseractTrigger;
import admin.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
public class TesseractScheduler implements InitializingBean {
    @Autowired
    private ITesseractTriggerService tesseractTriggerService;

    @Autowired
    private TesseractTriggerDispatcher tesseractTriggerDispatcher;


    private int maxBatchSize = 50;
    private int timeWindowSize = 10;
    private int sleepTime = 20;

    private Thread schedulerThread;


    public void destroy() {
        schedulerThread.interrupt();
    }

    @Override
    public void afterPropertiesSet() {
        //开启线程
        schedulerThread = new Thread(new TriggerTaskRunnable());
        schedulerThread.start();
        log.info("schedulerThread start");
    }

    private class TriggerTaskRunnable implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
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
