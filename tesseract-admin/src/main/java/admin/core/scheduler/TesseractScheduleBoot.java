package admin.core.scheduler;

import admin.core.scanner.ExecutorScanner;
import admin.core.scheduler.pool.DefaultSchedulerThreadPool;
import admin.core.scheduler.pool.ISchedulerThreadPool;
import admin.entity.TesseractGroup;
import admin.entity.TesseractTrigger;
import admin.service.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import feignService.IAdminFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;
import tesseract.exception.TesseractException;

import java.util.List;
import java.util.Map;

@Slf4j
public class TesseractScheduleBoot {
    private final static String DEFAULT_GROUP_NAME = "defaultGroup";
    private final static Integer DEFAULT_GROUP_THREAD_NUN = 10;
    @Autowired
    private ITesseractTriggerService tesseractTriggerService;

    @Autowired
    private ITesseractExecutorDetailService executorDetailService;

    @Autowired
    private ITesseractJobDetailService tesseractJobDetailService;
    @Autowired
    private ITesseractLogService tesseractLogService;

    @Autowired
    private ITesseractExecutorService executorService;

    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;

    @Autowired
    private IAdminFeignService feignService;

    @Autowired
    private ITesseractGroupService groupService;

    /**
     * threadlist
     */
    private List<SchedulerThread> schedulerThreadList = Lists.newArrayList();

    private List<TesseractTriggerDispatcher> tesseractTriggerDispatcherList = Lists.newArrayList();

    private static Map<String, ISchedulerThreadPool> threadPoolMap = Maps.newHashMap();

    private static Map<String, TesseractTriggerDispatcher> triggerDispatcherHashMap = Maps.newHashMap();
    /**
     * 暂时先共用同一扫描器
     */
    private ExecutorScanner executorScanner;

    public void destroy() {
        schedulerThreadList.forEach(schedulerThread -> schedulerThread.stopThread());
        tesseractTriggerDispatcherList.forEach(triggerDispatcher -> triggerDispatcher.stop());
        executorScanner.stopThread();
    }

    @EventListener(ContextRefreshedEvent.class)
    public void start() {
        schedulerThreadList.forEach(schedulerThread -> schedulerThread.startThread());
        executorScanner.startThread();
    }

    public void init() {
        //创建扫描线程
        executorScanner = new ExecutorScanner(executorDetailService);
        executorScanner.setDaemon(true);
        //创建调度线程,根据部门进行线程池隔离
        List<TesseractGroup> groupList = groupService.list();
        if (!CollectionUtils.isEmpty(groupList)) {
            groupList.forEach(group -> {
                String groupName = group.getName();
                Integer threadPoolNum = group.getThreadPoolNum();
                TesseractTriggerDispatcher tesseractTriggerDispatcher = createTesseractTriggerDispatcher(groupName, threadPoolNum);
                SchedulerThread schedulerThread = new SchedulerThread(groupName, tesseractTriggerDispatcher, tesseractTriggerService);
                schedulerThread.setDaemon(true);
                triggerDispatcherHashMap.put(groupName, tesseractTriggerDispatcher);
                schedulerThreadList.add(schedulerThread);
                tesseractTriggerDispatcherList.add(tesseractTriggerDispatcher);
            });
            return;
            //如果没有发现创建组，则默认创建default组调度器
        }
        TesseractTriggerDispatcher tesseractTriggerDispatcher = createTesseractTriggerDispatcher(DEFAULT_GROUP_NAME, DEFAULT_GROUP_THREAD_NUN);
        SchedulerThread schedulerThread = new SchedulerThread(DEFAULT_GROUP_NAME, tesseractTriggerDispatcher, tesseractTriggerService);
        schedulerThread.setDaemon(true);
        schedulerThreadList.add(schedulerThread);
        tesseractTriggerDispatcherList.add(tesseractTriggerDispatcher);
    }

    /**
     * 创建TesseractTriggerDispatcher
     *
     * @param groupName
     * @param threadNum
     * @return
     */
    private TesseractTriggerDispatcher createTesseractTriggerDispatcher(String groupName, Integer threadNum) {
        DefaultSchedulerThreadPool threadPool = new DefaultSchedulerThreadPool(threadNum);
        threadPoolMap.put(groupName, threadPool);
        TesseractTriggerDispatcher tesseractTriggerDispatcher = new TesseractTriggerDispatcher();
        tesseractTriggerDispatcher.setGroupName(groupName);
        tesseractTriggerDispatcher.setExecutorDetailService(executorDetailService);
        tesseractTriggerDispatcher.setExecutorService(executorService);
        tesseractTriggerDispatcher.setFeignService(feignService);
        tesseractTriggerDispatcher.setFiredTriggerService(firedTriggerService);
        tesseractTriggerDispatcher.setTesseractJobDetailService(tesseractJobDetailService);
        tesseractTriggerDispatcher.setTesseractLogService(tesseractLogService);
        tesseractTriggerDispatcher.setThreadPool(threadPool);
        return tesseractTriggerDispatcher;
    }

    /**
     * static
     *
     * @param tesseractTriggerList
     */
    public static void executeTrigger(String groupName, List<TesseractTrigger> tesseractTriggerList) {
        TesseractTriggerDispatcher tesseractTriggerDispatcher = triggerDispatcherHashMap.get(groupName);
        if (tesseractTriggerDispatcher == null) {
            log.error("找不到组:{} TesseractTriggerDispatcher", groupName);
            throw new TesseractException("找不到TesseractTriggerDispatcher");
        }
        tesseractTriggerDispatcher.dispatchTrigger(tesseractTriggerList, true);
    }

}
