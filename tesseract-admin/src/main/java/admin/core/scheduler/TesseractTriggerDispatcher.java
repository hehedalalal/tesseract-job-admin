package admin.core.scheduler;

import admin.core.scheduler.feignService.IAdminFeignService;
import admin.core.scheduler.router.impl.HashRouter;
import admin.entity.*;
import admin.service.ITesseractExecutorService;
import admin.service.ITesseractExecutorTriggerLinkService;
import admin.service.ITesseractJobDetailService;
import admin.service.ITesseractLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static admin.constant.AdminConstant.*;
import static tesseract.core.constant.CommonConstant.EXECUTE_MAPPING;

@Slf4j
@AllArgsConstructor
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
    private IAdminFeignService feignService;


    private final String THREAD_NAME_FORMATTER = "TesseractSchedulerThread-%d";
    private final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(10,
            30, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(500)
            , r -> new Thread(String.format(THREAD_NAME_FORMATTER, ATOMIC_INTEGER.getAndIncrement())), (r, executor) -> {
        log.error("调度线程阻塞，检查网络设置");
        r.run();
    });

    public void dispachTrigger(List<TesseractTrigger> triggerList) {
        triggerList.stream().parallel().forEach(trigger -> {
            THREAD_POOL_EXECUTOR.execute(new TaskRunnable(trigger));
        });
    }

    @Data
    @AllArgsConstructor
    private class TaskRunnable implements Runnable {
        private TesseractTrigger trigger;

        @Override
        public void run() {
            try {
                TesseractLog tesseractLog = new TesseractLog();
                tesseractLog.setCreateTime(System.currentTimeMillis());
                tesseractLog.setCreator("test");
                tesseractLog.setTriggerName(trigger.getName());
                //获取job detail
                QueryWrapper<TesseractJobDetail> jobQueryWrapper = new QueryWrapper<>();
                TesseractJobDetail jobDetail = tesseractJobDetailService.getOne(jobQueryWrapper);
                if (jobDetail == null) {
                    tesseractLog.setJobName(NULL_JOB_NAME);
                    tesseractLog.setStatus(LOG_FAIL);
                    tesseractLog.setMsg("没有发现可运行job");
                    tesseractLog.setSocket(NULL_SOCKET);
                    tesseractLogService.save(tesseractLog);
                    return;
                }
                tesseractLog.setJobName(jobDetail.getName());
                //获取执行器
                QueryWrapper<TesseractExecutorTriggerLink> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(TesseractExecutorTriggerLink::getTriggerId, trigger.getId());
                List<TesseractExecutorTriggerLink> executorTriggerLinkList = triggerLinkService.list(queryWrapper);
                if (CollectionUtils.isEmpty(executorTriggerLinkList)) {
                    tesseractLog.setStatus(LOG_FAIL);
                    tesseractLog.setMsg("没有发现运行执行器");
                    tesseractLog.setSocket(NULL_SOCKET);
                    tesseractLogService.save(tesseractLog);
                    return;
                }
                //todo 广播
                //路由发送执行
                routerExecute(tesseractLog, executorTriggerLinkList, jobDetail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void routerExecute(TesseractLog tesseractLog, List<TesseractExecutorTriggerLink> executorTriggerLinkList, TesseractJobDetail jobDetail) {
            TesseractExecutorTriggerLink executorTriggerLink = SCHEDULE_ROUTER_MAP.getOrDefault(trigger.getStrategy(), new HashRouter()).routerExecutor(executorTriggerLinkList);
            TesseractExecutor tesseractExecutor = executorService.getById(executorTriggerLink.getExecutorId());
            tesseractLog.setSocket(tesseractExecutor.getSocket());
            //构建请求
            TesseractExecutorRequest executorRequest = new TesseractExecutorRequest();
            executorRequest.setClassName(jobDetail.getClassName());
            executorRequest.setShardingIndex(trigger.getShardingNum());
            //发送调度请求
            TesseractExecutorResponse response = null;
            try {
                response = feignService.sendToExecutor(new URI(tesseractExecutor.getSocket() + EXECUTE_MAPPING), executorRequest);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            if (response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS) {
                tesseractLog.setStatus(LOG_WAIT);
            } else {
                tesseractLog.setStatus(LOG_FAIL);
            }
            tesseractLog.setMsg(response.getMsg());
            tesseractLogService.save(tesseractLog);
        }


    }

    public void destroy() {
        THREAD_POOL_EXECUTOR.shutdown();
    }
}