package admin.core.scheduler;

import admin.core.scheduler.router.impl.HashRouter;
import admin.entity.*;
import admin.service.*;
import admin.util.AdminUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import feignService.IAdminFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    private ITesseractExecutorTriggerLinkService triggerLinkService;
    @Autowired
    private ITesseractExecutorService executorService;
    @Autowired
    private ITesseractTriggerService triggerService;
    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;
    @Autowired
    private IAdminFeignService feignService;


    private final String THREAD_NAME_FORMATTER = "TesseractSchedulerThread-%d";
    private final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(10,
            30, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500)
            , r -> new Thread(r, String.format(THREAD_NAME_FORMATTER, ATOMIC_INTEGER.getAndIncrement())), (r, executor) -> {
        log.error("调度线程阻塞，检查网络设置");
        r.run();
    });

    public void dispatchTrigger(List<TesseractTrigger> triggerList, boolean isOnce) {
        triggerList.parallelStream().forEach(trigger -> {
            //更新触发器下一次执行时间
            trigger.setPrevTriggerTime(System.currentTimeMillis());
            triggerService.updateTriggerStatusAndCalculate(Arrays.asList(trigger), TRGGER_STATUS_STARTING);
            THREAD_POOL_EXECUTOR.execute(new TaskRunnable(trigger, isOnce));
        });
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
                //将触发器加入fired_trigger
                TesseractFiredTrigger tesseractFiredTrigger = new TesseractFiredTrigger();
                tesseractFiredTrigger.setCreateTime(System.currentTimeMillis());
                tesseractFiredTrigger.setName(trigger.getName());
                tesseractFiredTrigger.setTriggerId(trigger.getId());
                firedTriggerService.save(tesseractFiredTrigger);
                //构建日志
                TesseractLog tesseractLog = new TesseractLog();
                tesseractLog.setClassName("");
                tesseractLog.setCreateTime(System.currentTimeMillis());
                tesseractLog.setCreator("test");
                tesseractLog.setTriggerName(trigger.getName());
                tesseractLog.setEndTime(0L);
                //获取job detail
                QueryWrapper<TesseractJobDetail> jobQueryWrapper = new QueryWrapper<>();
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
                QueryWrapper<TesseractExecutorTriggerLink> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(TesseractExecutorTriggerLink::getTriggerId, trigger.getId());
                List<TesseractExecutorTriggerLink> executorTriggerLinkList = triggerLinkService.list(queryWrapper);
                if (CollectionUtils.isEmpty(executorTriggerLinkList)) {
                    tesseractLog.setStatus(LOG_FAIL);
                    tesseractLog.setMsg("没有发现运行执行器");
                    tesseractLog.setSocket(NULL_SOCKET);
                    tesseractLog.setEndTime(System.currentTimeMillis());
                    tesseractLogService.save(tesseractLog);
                    log.info("tesseractLog:{}", tesseractLog);
                    return;
                }
                //todo 广播
                //路由发送执行
                routerExecute(tesseractLog, executorTriggerLinkList, jobDetail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 根据路由策略，选择机器执行
         *
         * @param tesseractLog
         * @param executorTriggerLinkList
         * @param jobDetail
         */
        private void routerExecute(TesseractLog tesseractLog, List<TesseractExecutorTriggerLink> executorTriggerLinkList, TesseractJobDetail jobDetail) {
            TesseractExecutorTriggerLink executorTriggerLink = SCHEDULE_ROUTER_MAP.getOrDefault(trigger.getStrategy(), new HashRouter()).routerExecutor(executorTriggerLinkList);
            TesseractExecutor tesseractExecutor = executorService.getById(executorTriggerLink.getExecutorId());
            //首先保存日志，获取到日志id，便于异步更新
            tesseractLog.setSocket(tesseractExecutor.getSocket());
            tesseractLog.setStatus(LOG_INIT);
            tesseractLog.setMsg("等待执行器执行");
            tesseractLogService.save(tesseractLog);
            //构建请求
            TesseractExecutorRequest executorRequest = new TesseractExecutorRequest();
            executorRequest.setClassName(jobDetail.getClassName());
            executorRequest.setShardingIndex(trigger.getShardingNum());
            executorRequest.setLogId(tesseractLog.getId());
            executorRequest.setTriggerId(trigger.getId());
            //发送调度请求
            TesseractExecutorResponse response;
            try {
                response = feignService.sendToExecutor(new URI(HTTP_PREFIX + tesseractExecutor.getSocket() + EXECUTE_MAPPING), executorRequest);
            } catch (URISyntaxException e) {
                log.error("URI异常:{}", e.getMessage());
                response = TesseractExecutorResponse.builder().body("URI异常").status(TesseractExecutorResponse.FAIL_STAUTS).build();
            }
            //如果执行失败则更新日志状态并且移出执行表，如果執行成功则异步由执行器修改状态和移出執行表
            if (response.getStatus() != TesseractExecutorResponse.SUCCESS_STATUS) {
                tesseractLog.setStatus(LOG_FAIL);
            } else {
                return;
            }
            tesseractLog.setEndTime(System.currentTimeMillis());
            Object body = response.getBody();
            if (body != null) {
                tesseractLog.setMsg(body.toString());
            }
            //移出执行表并修改日志状态
            firedTriggerService.removeFiredTriggerAndUpdateLog(trigger.getId(), tesseractLog);
            log.info("tesseractLog:{}", tesseractLog);
        }

    }

    public void destroy() {
        THREAD_POOL_EXECUTOR.shutdown();
    }
}