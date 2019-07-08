package admin.core.scheduler;

import admin.constant.AdminConstant;
import admin.core.scheduler.pool.DefaultSchedulerThreadPool;
import admin.core.scheduler.pool.ISchedulerThreadPool;
import admin.core.scheduler.router.impl.HashRouter;
import admin.entity.*;
import admin.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import feignService.IAdminFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static admin.constant.AdminConstant.*;
import static tesseract.core.constant.CommonConstant.EXECUTE_MAPPING;
import static tesseract.core.constant.CommonConstant.HTTP_PREFIX;

@Slf4j
public class TesseractTriggerDispatcher {
    @Autowired
    private ITesseractJobDetailService tesseractJobDetailService;
    @Autowired
    private ITesseractLogService tesseractLogService;
    @Autowired
    private ITesseractExecutorDetailService executorDetailService;
    @Autowired
    private ITesseractExecutorService executorService;
    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;
    @Autowired
    private IAdminFeignService feignService;
    private ISchedulerThreadPool threadPool = new DefaultSchedulerThreadPool(500);

//    private final String THREAD_NAME_FORMATTER = "TesseractSchedulerThread-%d";
//    private final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
//    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(10,
//            100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500)
//            , r -> {
//        Thread thread = new Thread(r, String.format(THREAD_NAME_FORMATTER, ATOMIC_INTEGER.getAndIncrement()));
//        thread.setDaemon(true);
//        return thread;
//    }, (r, executor) ->
//
//    {
//        log.error("任务数过多，调度线程将阻塞，任务可能无法调度，检查网络设置");
//        r.run();
//    });

    public void dispatchTrigger(List<TesseractTrigger> triggerList, boolean isOnce) {
        //triggerList.stream().forEach(trigger -> THREAD_POOL_EXECUTOR.execute(new TaskRunnable(trigger, isOnce)));
        triggerList.stream().forEach(trigger -> threadPool.runJob(new TaskRunnable(trigger, isOnce)));
    }

    public int blockGetAvailableThreadNum() {
        return threadPool.blockGetAvailableThreadNum();
    }

    public void init() {
        threadPool.init();
    }

    private class TaskRunnable implements Runnable {
        private TesseractTrigger trigger;
        private boolean isOnce;

        public TaskRunnable(TesseractTrigger trigger, boolean isOnce) {
            this.trigger = trigger;
            this.isOnce = isOnce;
        }

        @Override
        public void run() {
            try {
                //构建日志
                TesseractLog tesseractLog = new TesseractLog();
                tesseractLog.setClassName("");
                tesseractLog.setCreateTime(System.currentTimeMillis());
                tesseractLog.setCreator("test");
                tesseractLog.setTriggerName(trigger.getName());
                tesseractLog.setEndTime(0L);
                //获取job detail
                QueryWrapper<TesseractJobDetail> jobQueryWrapper = new QueryWrapper<>();
                jobQueryWrapper.lambda().eq(TesseractJobDetail::getTriggerId, trigger.getId());
                TesseractJobDetail jobDetail = tesseractJobDetailService.getOne(jobQueryWrapper);
                if (jobDetail == null) {
                    tesseractLog.setStatus(LOG_FAIL);
                    tesseractLog.setMsg("没有发现可运行job");
                    tesseractLog.setSocket(NULL_SOCKET);
                    tesseractLog.setEndTime(System.currentTimeMillis());
                    log.info("tesseractLog:{}", tesseractLog);
                    tesseractLogService.save(tesseractLog);
                    return;
                }
                tesseractLog.setClassName(jobDetail.getClassName());
                //获取执行器
                TesseractExecutor executor = executorService.getById(trigger.getExecutorId());
                if (executor == null) {
                    tesseractLog.setStatus(LOG_FAIL);
                    tesseractLog.setMsg("没有找到可用执行器");
                    tesseractLog.setSocket(NULL_SOCKET);
                    tesseractLog.setEndTime(System.currentTimeMillis());
                    tesseractLogService.save(tesseractLog);
                    log.info("tesseractLog:{}", tesseractLog);
                    return;
                }
                QueryWrapper<TesseractExecutorDetail> executorDetailQueryWrapper = new QueryWrapper<>();
                List<TesseractExecutorDetail> executorDetailList = executorDetailService.list(executorDetailQueryWrapper);
                if (CollectionUtils.isEmpty(executorDetailList)) {
                    tesseractLog.setStatus(LOG_FAIL);
                    tesseractLog.setMsg("执行器下没有可用机器");
                    tesseractLog.setSocket(NULL_SOCKET);
                    tesseractLog.setEndTime(System.currentTimeMillis());
                    tesseractLogService.save(tesseractLog);
                    log.info("tesseractLog:{}", tesseractLog);
                    return;
                }
                //todo 广播
                //路由发送执行
                routerExecute(tesseractLog, executorDetailList, jobDetail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 根据路由策略，选择机器执行
         *
         * @param tesseractLog
         * @param executorDetailList
         * @param jobDetail
         */
        private void routerExecute(TesseractLog tesseractLog, List<TesseractExecutorDetail> executorDetailList, TesseractJobDetail jobDetail) {
            TesseractExecutorDetail executorDetail = SCHEDULE_ROUTER_MAP.getOrDefault(trigger.getStrategy(), new HashRouter()).routerExecutor(executorDetailList);
            //首先保存日志，获取到日志id，便于异步更新
            tesseractLog.setSocket(executorDetail.getSocket());
            tesseractLog.setMsg("执行中");
            tesseractLog.setStatus(AdminConstant.LOG_WAIT);
            tesseractLogService.save(tesseractLog);
            //将触发器加入fired_trigger
            TesseractFiredTrigger tesseractFiredTrigger = new TesseractFiredTrigger();
            tesseractFiredTrigger.setCreateTime(System.currentTimeMillis());
            tesseractFiredTrigger.setName(trigger.getName());
            tesseractFiredTrigger.setTriggerId(trigger.getId());
            tesseractFiredTrigger.setClassName(jobDetail.getClassName());
            tesseractFiredTrigger.setExecutorId(executorDetail.getExecutorId());
            tesseractFiredTrigger.setSocket(executorDetail.getSocket());
            firedTriggerService.save(tesseractFiredTrigger);
            //构建请求
            TesseractExecutorRequest executorRequest = new TesseractExecutorRequest();
            executorRequest.setClassName(jobDetail.getClassName());
            executorRequest.setShardingIndex(trigger.getShardingNum());
            executorRequest.setLogId(tesseractLog.getId());
            executorRequest.setTriggerId(trigger.getId());
            executorRequest.setExecutorId(executorDetail.getExecutorId());
            //发送调度请求
            TesseractExecutorResponse response = TesseractExecutorResponse.FAIL;
            log.info("开始调度:{}", executorRequest);
            try {
                response = feignService.sendToExecutor(new URI(HTTP_PREFIX + executorDetail.getSocket() + EXECUTE_MAPPING), executorRequest);
            } catch (URISyntaxException e) {
                log.error("URI异常:{}", e.getMessage());
                response = TesseractExecutorResponse.builder().body("URI异常").status(TesseractExecutorResponse.FAIL_STAUTS).build();
            }
            //如果执行失败则更新日志状态并且移出执行表，如果執行成功则异步由执行器修改状态和移出執行表
            if (response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS) {
                return;
            }
            tesseractLog.setStatus(AdminConstant.LOG_FAIL);
            tesseractLog.setEndTime(System.currentTimeMillis());
            Object body = response.getBody();
            if (body != null) {
                tesseractLog.setMsg(body.toString());
            }
            //移出执行表并修改日志状态
            firedTriggerService.removeFiredTriggerAndUpdateLog(trigger.getId(), executorDetail.getId(), tesseractLog);
            log.info("tesseractLog:{}", tesseractLog);
        }

    }

    public void stop() {
//        THREAD_POOL_EXECUTOR.shutdownNow();
//        THREAD_POOL_EXECUTOR.awaitTermination(1, TimeUnit.DAYS);
        threadPool.shutdown();
    }
}