package admin.service.impl;

import admin.entity.TesseractExecutor;
import admin.entity.TesseractJobDetail;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractExecutorMapper;
import admin.service.ITesseractExecutorService;
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
import tesseract.exception.TesseractException;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private ITesseractJobDetailService jobDetailService;

    @Override
    public void registry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception {
        @NotBlank String ip = tesseractAdminRegistryRequest.getIp();
        @NotNull Integer port = tesseractAdminRegistryRequest.getPort();
        String socket = ip + ":" + port;
        checkSocket(socket);
        toRegistry(socket, tesseractAdminRegistryRequest.getTesseractAdminJobDetailDTOList());
    }

    private void toRegistry(String socket, List<TesseractAdminJobDetailDTO> tesseractAdminJobDetailDTOList) {
        // 注册主机
        TesseractExecutor executor = new TesseractExecutor();
        executor.setCreateTime(System.currentTimeMillis());
        executor.setName("default");
        executor.setSocket(socket);
        executor.setUpdateTime(System.currentTimeMillis());
        this.save(executor);
        final List<String> notTriggerNameList = Lists.newArrayList();
        List<TesseractJobDetail> jobDetailList = Lists.newArrayList();
        //保存job
        tesseractAdminJobDetailDTOList.parallelStream().forEach(tesseractAdminJobDetailDTO -> {
                    @NotBlank String className = tesseractAdminJobDetailDTO.getClassName();
                    @NotBlank String triggerName = tesseractAdminJobDetailDTO.getTriggerName();
                    @NotBlank String jobName = tesseractAdminJobDetailDTO.getJobName();
                    @NotBlank String description = tesseractAdminJobDetailDTO.getDescription();
                    //检测trigger
                    QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(TesseractTrigger::getName, triggerName);
                    if (tesseractTriggerService.getOne(queryWrapper) == null) {
                        log.warn("触发器{}不存在", triggerName);
                        notTriggerNameList.add(triggerName);
                        return;
                    }
                    TesseractJobDetail jobDetail = new TesseractJobDetail();
                    jobDetail.setClassName(className);
                    jobDetail.setCreator("admin");
                    jobDetail.setName(jobName);
                    jobDetail.setDescription(description);
                    jobDetail.setCreateTime(System.currentTimeMillis());
                    jobDetailList.add(jobDetail);
                }
        );
        jobDetailService.saveBatch(jobDetailList);
    }

    /**
     * 以防重复注册
     *
     * @param socket
     */
    private void checkSocket(String socket) {
        QueryWrapper<TesseractExecutor> queryWrapper = new QueryWrapper<>();
        TesseractExecutor executor = getOne(queryWrapper);
        if (executor != null) {
            log.error("重复注册:{}", executor);
            throw new TesseractException("重复注册");
        }
    }
}
