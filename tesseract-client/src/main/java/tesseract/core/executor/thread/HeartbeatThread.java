package tesseract.core.executor.thread;

import lombok.extern.slf4j.Slf4j;
import tesseract.core.dto.TesseractExecutorResponse;
import tesseract.core.dto.TesseractHeartbeatRequest;
import tesseract.core.executor.TesseractExecutor;
import tesseract.core.lifecycle.IThreadLifycycle;
import tesseract.feignService.IClientFeignService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ThreadPoolExecutor;

import static tesseract.core.constant.CommonConstant.*;

@Slf4j
public class HeartbeatThread extends Thread implements IThreadLifycycle {
    private volatile boolean isStop = false;
    private volatile boolean isPause = true;

    private IClientFeignService clientFeignService;
    private String adminServerAddress;
    private String ip;
    private Integer port;
    private RegistryThread registryThread;
    private Integer heartIntervalTime = 10 * 1000;
    private TesseractExecutor tesseractExecutor;

    public HeartbeatThread(IClientFeignService clientFeignService, String adminServerAddress, String ip, Integer port) {
        super("HeartbeatThread");
        this.clientFeignService = clientFeignService;
        this.adminServerAddress = adminServerAddress;
        this.ip = ip;
        this.port = port;
    }

    public void setRegistryThread(RegistryThread registryThread) {
        this.registryThread = registryThread;
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
        isStop = true;
        this.interrupt();
    }

    @Override
    public void run() {
        log.info("HeartbeatThread start");
        while (!isStop) {
            if (isPause) {
                try {
                    /**
                     * 暂停情况下开始睡觉
                     */
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                }
                continue;
            }
            //开始心跳
            heartbeat();
            try {
                Thread.sleep(heartIntervalTime);
            } catch (InterruptedException e) {
            }
        }
    }

    private void heartbeat() {
        try {
            TesseractHeartbeatRequest tesseractHeartbeatRequest = new TesseractHeartbeatRequest();
            ThreadPoolExecutor threadPoolExecutor = tesseractExecutor.getThreadPoolExecutor();
            int activeCount = threadPoolExecutor.getActiveCount();
            int corePoolSize = threadPoolExecutor.getCorePoolSize();
            int maximumPoolSize = threadPoolExecutor.getMaximumPoolSize();
            int poolSize = threadPoolExecutor.getPoolSize();
            int queueSize = threadPoolExecutor.getQueue().size();
            tesseractHeartbeatRequest.setActiveCount(activeCount);
            tesseractHeartbeatRequest.setCorePoolSize(corePoolSize);
            tesseractHeartbeatRequest.setMaximumPoolSize(maximumPoolSize);
            tesseractHeartbeatRequest.setPoolSize(poolSize);
            tesseractHeartbeatRequest.setQueueSize(queueSize);
            tesseractHeartbeatRequest.setSocket(String.format(SOCKET_FORMATTER, ip, port));
            TesseractExecutorResponse response = clientFeignService.heartbeat(new URI(adminServerAddress + HEARTBEAT_MAPPING), tesseractHeartbeatRequest);
            if (response.getStatus() == TesseractExecutorResponse.SUCCESS_STATUS) {
                log.info("心跳成功");
                return;
            }
            if (response.getStatus() == EXECUTOR_DETAIL_NOT_FIND) {
                log.info("机器已失效，将重新注册", response);
                this.isPause = true;
                registryThread.startRegistry();
                return;
            }
            log.error("心跳失败:{}", response);
        } catch (URISyntaxException e) {
            log.error("uri信息错误，请检查配置");
        } catch (Exception e) {
            e.printStackTrace();
            log.error("心跳失败:{}", e.getMessage());
        }
    }

    /**
     * 开始心跳
     */
    public void startHeartbeat() {
        this.isPause = false;
        this.interrupt();
    }
}
