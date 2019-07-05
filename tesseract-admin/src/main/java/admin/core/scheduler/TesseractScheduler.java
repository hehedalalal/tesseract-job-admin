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
                List<TesseractTrigger> triggerList = tesseractTriggerService.findTriggerWithLock(maxBatchSize, System.currentTimeMillis());
                log.info("schedulerThread扫描到触发器：{}", triggerList);
                if (!CollectionUtils.isEmpty(triggerList)) {
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
