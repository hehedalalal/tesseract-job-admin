package admin.core.scheduler;

import admin.entity.TesseractTrigger;
import admin.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import tesseract.core.lifecycle.IThreadLifycycle;

import java.util.List;

@Slf4j
public class SchedulerThread extends Thread implements IThreadLifycycle {
    private volatile boolean isStop = false;
    private TesseractTriggerDispatcher tesseractTriggerDispatcher;
    private ITesseractTriggerService tesseractTriggerService;
    private int timeWindowSize = 5 * 1000;
    private int sleepTime = 20 * 1000;
    private int accurateTime = 1 * 1000;
    private String groupName;

    public SchedulerThread(String groupName, TesseractTriggerDispatcher tesseractTriggerDispatcher, ITesseractTriggerService tesseractTriggerService) {
        this.tesseractTriggerDispatcher = tesseractTriggerDispatcher;
        this.tesseractTriggerService = tesseractTriggerService;
        this.setName(String.format("SchedulerThread-%s", groupName));
        this.groupName = groupName;
    }

    @Override
    public void run() {
        log.info("SchedulerThread {} start", groupName);
        while (!isStop) {
            int blockGetAvailableThreadNum = tesseractTriggerDispatcher.blockGetAvailableThreadNum();
            log.info("可用线程数:{}", blockGetAvailableThreadNum);
            List<TesseractTrigger> triggerList = tesseractTriggerService.findTriggerWithLock(groupName, blockGetAvailableThreadNum, System.currentTimeMillis(), timeWindowSize);
            log.info("扫描触发器数量:{}", triggerList.size());
            if (!CollectionUtils.isEmpty(triggerList)) {
                //降序排序等待时间差
                TesseractTrigger tesseractTrigger = triggerList.get(0);
                Long nextTriggerTime = tesseractTrigger.getNextTriggerTime();
                long time = nextTriggerTime - System.currentTimeMillis();
                if (time > accurateTime) {
                    synchronized (this) {
                        try {
                            this.wait(time);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                tesseractTriggerDispatcher.dispatchTrigger(triggerList, false);
                continue;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void initThread() {

    }

    @Override
    public void startThread() {
        this.start();
    }

    @Override
    public void stopThread() {
        this.isStop = true;
        this.interrupt();
    }
}