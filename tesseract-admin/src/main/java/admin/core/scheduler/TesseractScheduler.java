package admin.core.scheduler;

import admin.core.scanner.ExecutorScanner;
import admin.service.ITesseractExecutorDetailService;
import admin.service.ITesseractTriggerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Slf4j
public class TesseractScheduler {
    @Autowired
    private ITesseractTriggerService tesseractTriggerService;

    @Autowired
    private TesseractTriggerDispatcher tesseractTriggerDispatcher;

    @Autowired
    private ITesseractExecutorDetailService executorDetailService;


    /**
     * threads
     */
    private SchedulerThread schedulerThread;
    private ExecutorScanner executorScanner;

    public void destroy() {
        schedulerThread.stopThread();
        tesseractTriggerDispatcher.stop();
        executorScanner.stopThread();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        schedulerThread.startThread();
        executorScanner.startThread();
    }

    public void init() {
        //创建线程
        schedulerThread = new SchedulerThread(tesseractTriggerDispatcher, tesseractTriggerService);
        executorScanner = new ExecutorScanner(executorDetailService);
        executorScanner.setDaemon(true);
        schedulerThread.setDaemon(true);
    }


}
