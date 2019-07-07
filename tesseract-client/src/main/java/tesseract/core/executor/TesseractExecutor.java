package tesseract.core.executor;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import tesseract.core.annotation.ClientJobDetail;
import tesseract.core.context.ExecutorContext;
import tesseract.core.dto.*;
import tesseract.core.handler.JobHandler;
import tesseract.exception.TesseractException;
import tesseract.feignService.IClientFeignService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static tesseract.core.constant.CommonConstant.*;


@Slf4j
public class TesseractExecutor implements InitializingBean, DisposableBean {
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
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(10,
            100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(800)
            , r -> new Thread(r, String.format(THREAD_NAME_FORMATTER, ATOMIC_INTEGER.getAndIncrement())));
    /**
     * constant
     */
    private Thread registryThread;

    /**
     * 开始执行任务，扔到线程池后发送成功执行通知，执行完毕后发送异步执行成功通知
     *
     * @param tesseractExecutorRequest
     * @return
     */
    public TesseractExecutorResponse execute(TesseractExecutorRequest tesseractExecutorRequest) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            String className = tesseractExecutorRequest.getClassName();
            TesseractAdminJobNotify tesseractAdminJobNotify = new TesseractAdminJobNotify();
            tesseractAdminJobNotify.setLogId(tesseractExecutorRequest.getLogId());
            tesseractAdminJobNotify.setTriggerId(tesseractExecutorRequest.getTriggerId());
            tesseractAdminJobNotify.setExecutorId(tesseractExecutorRequest.getExecutorId());
            TesseractExecutorResponse notify = null;
            try {
                Class<?> aClass = Class.forName(className);
                JobHandler jobHandler = (JobHandler) aClass.newInstance();
                ExecutorContext executorContext = new ExecutorContext();
                executorContext.setShardingIndex(tesseractExecutorRequest.getShardingIndex());
                jobHandler.execute(executorContext);
                notify = clientFeignService.notify(new URI(getAdminServerAddress() + NOTIFY_MAPPING), tesseractAdminJobNotify);
            } catch (Exception e) {
                log.error("执行异常:{}", e.getMessage());
                tesseractAdminJobNotify.setException(e.getMessage());
                try {
                    notify = clientFeignService.notify(new URI(getAdminServerAddress() + NOTIFY_MAPPING), tesseractAdminJobNotify);
                } catch (URISyntaxException ex) {
                    log.error("执行异常URI异常:{}", e.getMessage());
                }
            }
            log.info("执行通知结果:{}", notify);
        });
        return TesseractExecutorResponse.builder().status(TesseractExecutorResponse.SUCCESS_STATUS).body("成功进入队列").build();
    }

    private String getAdminServerAddress() {
        return this.adminServerAddress;
    }

    /**
     * 属性初始化完毕后开始注册
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() {
        registryThread = new Thread(new RegistryRunnable());
        registryThread.start();
    }

    @Override
    public void destroy() throws Exception {
        registryThread.interrupt();
    }

    /**
     * 注册任务
     */
    private class RegistryRunnable implements Runnable {
        private volatile boolean isRegistry = false;
        private volatile boolean isStop = false;

        @Override
        public void run() {
            while (!isStop) {
                try {
                    if (!isRegistry) {
                        //注册
                        registry();
                    }
                    //每十秒钟检测一次
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    isRegistry = false;
                    log.error("注册失败:{}", e.getMessage());
                }
            }
        }

        private void registry() {
            try {
                log.info("开始注册");
                if (CollectionUtils.isEmpty(clientJobDetailList)) {
                    log.info("clientJobDetailList 为空，注册停止");
                    isStop = true;
                    return;
                }
                TesseractAdminRegistryRequest tesseractAdminRegistryRequest = buildRequest();
                log.info("注册中:{}", tesseractAdminRegistryRequest);
                TesseractExecutorResponse response = clientFeignService.registry(new URI(adminServerAddress + REGISTRY_MAPPING), tesseractAdminRegistryRequest);
                if (response.getStatus() != TesseractExecutorResponse.SUCCESS_STATUS) {
                    log.error("注册异常,状态码：{},信息：{}", response.getStatus(), response.getBody());
                    throw new TesseractException(response.getBody().toString());
                }
                log.info("注册成功,返回信息:{}", response.getBody());
                isRegistry = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 构建请求
         *
         * @return
         */
        private TesseractAdminRegistryRequest buildRequest() {
            TesseractAdminRegistryRequest tesseractAdminRegistryRequest = new TesseractAdminRegistryRequest();
            tesseractAdminRegistryRequest.setIp(ip);
            tesseractAdminRegistryRequest.setPort(port);
            List<TesseractAdminJobDetailDTO> detailDTOList = Collections.synchronizedList(Lists.newArrayList());
            if (!CollectionUtils.isEmpty(clientJobDetailList)) {
                clientJobDetailList.parallelStream().forEach(clientJobDetail -> {
                    TesseractAdminJobDetailDTO tesseractAdminJobDetailDTO = new TesseractAdminJobDetailDTO();
                    tesseractAdminJobDetailDTO.setClassName(clientJobDetail.getClassName());
                    tesseractAdminJobDetailDTO.setTriggerName(clientJobDetail.getTriggerName());
                    detailDTOList.add(tesseractAdminJobDetailDTO);
                });
            }
            tesseractAdminRegistryRequest.setTesseractAdminJobDetailDTOList(detailDTOList);
            return tesseractAdminRegistryRequest;
        }
    }
}
