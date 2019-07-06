package admin.service.impl;

import admin.entity.TesseractExecutor;
import admin.entity.TesseractExecutorTriggerLink;
import admin.entity.TesseractJobDetail;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractExecutorMapper;
import admin.service.ITesseractExecutorService;
import admin.service.ITesseractExecutorTriggerLinkService;
import admin.service.ITesseractJobDetailService;
import admin.service.ITesseractTriggerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tesseract.core.dto.TesseractAdminJobDetailDTO;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractAdminRegistryResDTO;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@Service
@Slf4j
@Transactional
public class TesseractExecutorServiceImpl extends ServiceImpl<TesseractExecutorMapper, TesseractExecutor> implements ITesseractExecutorService {
    @Autowired
    private ITesseractTriggerService tesseractTriggerService;
    @Autowired
    private ITesseractExecutorTriggerLinkService triggerLinkService;
    @Autowired
    private ITesseractJobDetailService jobDetailService;

    @Override
    public TesseractAdminRegistryResDTO registry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception {
        @NotBlank String ip = tesseractAdminRegistryRequest.getIp();
        @NotNull Integer port = tesseractAdminRegistryRequest.getPort();
        String socket = ip + ":" + port;
        return toRegistry(socket, tesseractAdminRegistryRequest.getTesseractAdminJobDetailDTOList());
    }

    private TesseractAdminRegistryResDTO toRegistry(String socket, List<TesseractAdminJobDetailDTO> tesseractAdminJobDetailDTOList) {
        // 注册主机
        final TesseractExecutor executor = registryExecutor(socket);
        final List<String> notTriggerNameList = Collections.synchronizedList(Lists.newArrayList());
        final List<String> repeatJobList = Collections.synchronizedList(Lists.newArrayList());
        List<TesseractJobDetail> jobDetailList = Collections.synchronizedList(Lists.newArrayList());
        //保存job
        tesseractAdminJobDetailDTOList.parallelStream().forEach(tesseractAdminJobDetailDTO -> {
                    @NotBlank String className = tesseractAdminJobDetailDTO.getClassName();
                    @NotBlank String triggerName = tesseractAdminJobDetailDTO.getTriggerName();
                    //检测trigger
                    QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(TesseractTrigger::getName, triggerName);
                    TesseractTrigger trigger = tesseractTriggerService.getOne(queryWrapper);
                    if (trigger == null) {
                        log.warn("触发器{}不存在", triggerName);
                        notTriggerNameList.add(triggerName);
                        return;
                    }
                    //找到trigger需要和executor绑定
                    bindExecutor(executor, trigger);
                    //以防任务重复注册，添加进job
                    QueryWrapper<TesseractJobDetail> jobDetailQueryWrapper = new QueryWrapper<>();
                    TesseractJobDetail jobDetail = jobDetailService.getOne(jobDetailQueryWrapper);
                    if (jobDetail != null) {
                        log.warn("重复任务{}", jobDetail);
                        repeatJobList.add(jobDetail.getClassName());
                        return;
                    }
                    jobDetail = new TesseractJobDetail();
                    jobDetail.setClassName(className);
                    jobDetail.setCreator(trigger.getCreator());
                    jobDetail.setTriggerId(trigger.getId());
                    jobDetail.setCreateTime(System.currentTimeMillis());
                    jobDetailList.add(jobDetail);
                }
        );
        // todo 由于多实例并发，这里插入任务需要加锁 目前占时采用 数据库唯一索引来保证不重复
        jobDetailService.saveBatch(jobDetailList);
        TesseractAdminRegistryResDTO tesseractAdminRegistryResDTO = new TesseractAdminRegistryResDTO();
        tesseractAdminRegistryResDTO.setNotTriggerNameList(notTriggerNameList);
        tesseractAdminRegistryResDTO.setRepeatJobList(repeatJobList);
        return tesseractAdminRegistryResDTO;
    }

    /**
     * 将触发器和执行器绑定
     *
     * @param executor
     */
    private void bindExecutor(TesseractExecutor executor, TesseractTrigger trigger) {
        //防止重复插入
        Integer executorId = executor.getId();
        Integer triggerId = trigger.getId();
        QueryWrapper<TesseractExecutorTriggerLink> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TesseractExecutorTriggerLink::getTriggerId, triggerId).eq(TesseractExecutorTriggerLink::getExecutorId, executorId);
        TesseractExecutorTriggerLink tesseractExecutorTriggerLink = triggerLinkService.getOne(queryWrapper);
        if (tesseractExecutorTriggerLink != null) {
            log.warn("执行器触发器关联{}已存在，将忽略注册", tesseractExecutorTriggerLink);
            return;
        }
        tesseractExecutorTriggerLink = new TesseractExecutorTriggerLink();
        tesseractExecutorTriggerLink.setExecutorId(executorId);
        tesseractExecutorTriggerLink.setTriggerId(triggerId);
        triggerLinkService.save(tesseractExecutorTriggerLink);
    }

    /**
     * 注册主机
     *
     * @param socket
     */
    private TesseractExecutor registryExecutor(String socket) {
        QueryWrapper<TesseractExecutor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TesseractExecutor::getSocket, socket);
        TesseractExecutor executor = getOne(queryWrapper);
        if (executor != null) {
            log.warn("执行器{}已存在，将忽略注册", executor);
            return executor;
        }
        executor = new TesseractExecutor();
        executor.setCreateTime(System.currentTimeMillis());
        executor.setName("default");
        executor.setSocket(socket);
        executor.setUpdateTime(System.currentTimeMillis());
        this.save(executor);
        return executor;
    }
}
