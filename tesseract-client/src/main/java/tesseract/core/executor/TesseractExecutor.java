package tesseract.core.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tesseract.core.context.ExecutorContext;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractExecutorRequest;
import tesseract.core.dto.TesseractExecutorResponse;
import tesseract.core.handler.JobHandler;
import tesseract.exception.TesseractException;
import tesseract.feignService.IClientFeignService;

import java.net.URI;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static tesseract.core.constant.CommonConstant.REGISTRY_MAPPING;


@Slf4j
public class TesseractExecutor implements InitializingBean, DisposableBean {
    @Autowired
    private IClientFeignService clientFeignService;
    @Value("${tesseract-admin-address}")
    private String adminServerAddress;
    /**
     * 线程池
     */
    private final String THREAD_NAME_FORMATTER = "TesseractExecutorThread-%d";
    private final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private final ThreadPoolExecutor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(10,
            100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(800)
            , r -> new Thread(String.format(THREAD_NAME_FORMATTER, ATOMIC_INTEGER.getAndIncrement())));
    /**
     * constant
     */
    private Thread registryThread;

    public TesseractExecutorResponse execute(TesseractExecutorRequest tesseractExecutorRequest) {
        THREAD_POOL_EXECUTOR.execute(() -> {
            String className = tesseractExecutorRequest.getClassName();
            try {
                Class<?> aClass = Class.forName(className);
                JobHandler jobHandler = (JobHandler) aClass.newInstance();
                ExecutorContext executorContext = new ExecutorContext();
                executorContext.setShardingIndex(tesseractExecutorRequest.getShardingIndex());
                jobHandler.execute(executorContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return TesseractExecutorResponse.SUCCESS;
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

    private class RegistryRunnable implements Runnable {
        private volatile boolean isRegistry = false;

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (!isRegistry) {
                        //注册
                        registry();
                    }
                    //每十秒钟检测一次
                    Thread.sleep(10 * 1000);
                } catch (Exception e) {
                    isRegistry = false;
                    log.error("注册失败:{}", e.getMessage());
                }
            }
        }

        private void registry() throws Exception {
            TesseractAdminRegistryRequest tesseractAdminRegistryRequest = new TesseractAdminRegistryRequest();
            TesseractExecutorResponse response = clientFeignService.registry(new URI(adminServerAddress + REGISTRY_MAPPING), tesseractAdminRegistryRequest);
            if (response.getStatus() != TesseractExecutorResponse.SUCCESS_STATUS) {
                log.error("注册异常,状态码：{},信息：{}", response.getStatus(), response.getStatus());
                throw new TesseractException(response.getMsg());
            }
            isRegistry = true;
        }
    }
}
