package tesseract.core.executor.thread;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import tesseract.core.annotation.ClientJobDetail;
import tesseract.core.dto.TesseractAdminJobDetailDTO;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractExecutorResponse;
import tesseract.core.lifecycle.IThreadLifycycle;
import tesseract.exception.TesseractException;
import tesseract.feignService.IClientFeignService;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static tesseract.core.constant.CommonConstant.REGISTRY_MAPPING;

/**
 * 注册任务
 *
 * @author nickle
 */
@Slf4j
public class RegistryThread extends Thread implements IThreadLifycycle {
    private volatile boolean isRegistry = false;
    private volatile boolean isStop = false;
    private IClientFeignService clientFeignService;
    private List<ClientJobDetail> clientJobDetailList;
    private String adminServerAddress;
    private String ip;
    private Integer port;
    private HeartbeatThread heartbeatThread;

    public RegistryThread(IClientFeignService clientFeignService, List<ClientJobDetail> clientJobDetailList, String adminServerAddress, String ip, Integer port) {
        super("RegistryThread");
        this.clientFeignService = clientFeignService;
        this.clientJobDetailList = clientJobDetailList;
        this.adminServerAddress = adminServerAddress;
        this.ip = ip;
        this.port = port;
    }

    public void setHeartbeatThread(HeartbeatThread heartbeatThread) {
        this.heartbeatThread = heartbeatThread;
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
        log.info("RegistryThread start");
        while (!isStop) {
            if (!isRegistry) {
                //注册
                registry();
            }
            try {
                //注册成功后开始睡觉
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException e) {
                log.info("中断");
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
            heartbeatThread.startHeartbeat();
        } catch (Exception e) {
            log.error("注册失败:{}", e.getMessage());
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

    /**
     * 将它从睡眠中唤醒注册
     */
    public void startRegistry() {
        isRegistry = false;
        this.interrupt();
    }
}