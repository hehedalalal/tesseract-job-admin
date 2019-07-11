package tesseract.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tesseract.core.annotation.ClientJobDetail;
import tesseract.core.context.ExecutorContext;
import tesseract.core.dto.TesseractAdminJobNotify;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;
import tesseract.core.executor.thread.HeartbeatThread;
import tesseract.core.executor.thread.RegistryThread;
import tesseract.core.handler.JobHandler;
import tesseract.feignService.IClientFeignService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static tesseract.core.constant.CommonConstant.NOTIFY_MAPPING;


@Slf4j
public class TesseractExecutor {
    @Autowired
    private IClientFeignService clientFeignService;
    @Value("${tesseract-admin-address}")
    private String adminServerAddress;
    @Value("${tesseract-executor-localIp}")
    private String ip;
    @Value("${server.port}")
    private Integer port;
    @Autowired(required = false)
    private List<ClientJobDetail> clientJobDetailList;
    /**
     * 线程池
     */
    private final String THREAD_NAME_FORMATTER = "TesseractExecutorThread-%d";
    private final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,
            800, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000)
            , r -> new Thread(r, String.format(THREAD_NAME_FORMATTER, ATOMIC_INTEGER.getAndIncrement())));
    /**
     * threads
     */
    //注册线程
    private RegistryThread registryThread;
    //心跳线程
    private HeartbeatThread heartbeatThread;

    /**
     * 开始执行任务，扔到线程池后发送成功执行通知，执行完毕后发送异步执行成功通知
     *
     * @param tesseractExecutorRequest
     * @return
     */
    public TesseractExecutorResponse execute(TesseractExecutorRequest tesseractExecutorRequest) {
        threadPoolExecutor.execute(new WorkRunnable(tesseractExecutorRequest, clientFeignService, this.adminServerAddress));
        return TesseractExecutorResponse.builder().status(TesseractExecutorResponse.SUCCESS_STATUS).body("成功进入队列").build();
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    /**
     * 属性初始化完毕后开始注册
     *
     * @throws Exception
     */
    @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
    public void init() {
        heartbeatThread = new HeartbeatThread(clientFeignService, adminServerAddress, ip, port);
        registryThread = new RegistryThread(clientFeignService, clientJobDetailList, adminServerAddress, ip, port);
        heartbeatThread.setDaemon(true);
        registryThread.setDaemon(true);
        heartbeatThread.setRegistryThread(registryThread);
        registryThread.setHeartbeatThread(heartbeatThread);
        registryThread.startThread();
        heartbeatThread.startThread();
    }

    public void destroy() {
        registryThread.stopThread();
        heartbeatThread.stopThread();
    }

    @Data
    @AllArgsConstructor
    private class WorkRunnable implements Runnable {
        private TesseractExecutorRequest tesseractExecutorRequest;
        private IClientFeignService clientFeignService;
        private String adminServerAddress;

        @Override
        public void run() {
            String className = tesseractExecutorRequest.getClassName();
            TesseractAdminJobNotify tesseractAdminJobNotify = new TesseractAdminJobNotify();
            tesseractAdminJobNotify.setLogId(tesseractExecutorRequest.getLogId());
            tesseractAdminJobNotify.setTriggerId(tesseractExecutorRequest.getTriggerId());
            tesseractAdminJobNotify.setExecutorId(tesseractExecutorRequest.getExecutorId());
            TesseractExecutorResponse notifyResponse = null;
            try {
                Class<?> aClass = Class.forName(className);
                JobHandler jobHandler = (JobHandler) aClass.newInstance();
                ExecutorContext executorContext = new ExecutorContext();
                executorContext.setShardingIndex(tesseractExecutorRequest.getShardingIndex());
                jobHandler.execute(executorContext);
                notifyResponse = clientFeignService.notify(new URI(adminServerAddress + NOTIFY_MAPPING), tesseractAdminJobNotify);
            } catch (Exception e) {
                log.error("执行异常:{}", e.getMessage());
                tesseractAdminJobNotify.setException(e.getMessage());
                try {
                    notifyResponse = clientFeignService.notify(new URI(adminServerAddress + NOTIFY_MAPPING), tesseractAdminJobNotify);
                } catch (URISyntaxException ex) {
                    log.error("执行异常URI异常:{}", e.getMessage());
                }
            }
            if (notifyResponse != null && notifyResponse.getStatus() != TesseractExecutorResponse.SUCCESS_STATUS) {
                log.error("通知执行器出错:{}", notifyResponse);
            }
            log.info("执行通知结果:{}", notifyResponse);
        }
    }

}
