package admin.service.impl;

import admin.entity.TesseractExecutor;
import admin.entity.TesseractExecutorDetail;
import admin.entity.TesseractJobDetail;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractExecutorMapper;
import admin.pojo.ExecutorVO;
import admin.pojo.PageVO;
import admin.pojo.TesseractExecutorVO;
import admin.service.ITesseractExecutorDetailService;
import admin.service.ITesseractExecutorService;
import admin.service.ITesseractJobDetailService;
import admin.service.ITesseractTriggerService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
    private ITesseractTriggerService triggerService;

    @Autowired
    private ITesseractExecutorService executorService;

    @Autowired
    private ITesseractExecutorDetailService executorDetailService;
    @Autowired
    private ITesseractJobDetailService jobDetailService;

    @Override
    public TesseractAdminRegistryResDTO registry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception {
        @NotBlank String ip = tesseractAdminRegistryRequest.getIp();
        @NotNull Integer port = tesseractAdminRegistryRequest.getPort();
        String socket = ip + ":" + port;
        return toRegistry(socket, tesseractAdminRegistryRequest.getTesseractAdminJobDetailDTOList());
    }

    @Override
    public ExecutorVO listByPage(Long currentPage, Long pageSize, TesseractExecutor condition,
                                 Long startCreateTime,
                                 Long endCreateTime) {
        ExecutorVO executorVO = new ExecutorVO();
        Page<TesseractExecutor> tesseractExecutorPage = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractExecutor> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TesseractExecutor> lambda = queryWrapper.lambda();
        //日期
        if (startCreateTime != null) {
            lambda.ge(TesseractExecutor::getCreateTime, startCreateTime);
        }

        if (endCreateTime != null) {
            lambda.le(TesseractExecutor::getCreateTime, endCreateTime);
        }

        //其他
        if (!StringUtils.isEmpty(condition.getName())) {
            lambda.like(TesseractExecutor::getName, condition.getName());
        }
        if (!StringUtils.isEmpty(condition.getCreator())) {
            lambda.like(TesseractExecutor::getCreator, condition.getCreator());
        }

        IPage<TesseractExecutor> page = page(tesseractExecutorPage, queryWrapper);
        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(currentPage);
        pageVO.setPageSize(pageSize);
        pageVO.setTotal(page.getTotal());
        List<TesseractExecutorVO> executorVOList = Collections.synchronizedList(Lists.newArrayList());
        List<TesseractExecutor> executorList = page.getRecords();
        executorList.parallelStream().forEach(executor -> {
            QueryWrapper<TesseractExecutorDetail> detailQueryWrapper = new QueryWrapper<>();
            detailQueryWrapper.lambda().eq(TesseractExecutorDetail::getExecutorId, executor.getId());
            List<TesseractExecutorDetail> executorDetailList = executorDetailService.list(detailQueryWrapper);
            TesseractExecutorVO tesseractExecutorVO = new TesseractExecutorVO();
            tesseractExecutorVO.setExecutor(executor);
            tesseractExecutorVO.setExecutorDetailList(executorDetailList);
            executorVOList.add(tesseractExecutorVO);
        });
        executorVO.setPageInfo(pageVO);
        executorVO.setExecutorList(executorVOList);
        return executorVO;
    }

    @Override
    public void saveExecutor(TesseractExecutor tesseractExecutor) {
        tesseractExecutor.setCreateTime(System.currentTimeMillis());
        tesseractExecutor.setCreator("admin");
        save(tesseractExecutor);
    }

    private TesseractAdminRegistryResDTO toRegistry(String socket, List<TesseractAdminJobDetailDTO> tesseractAdminJobDetailDTOList) {
        final List<String> repeatJobList = Collections.synchronizedList(Lists.newArrayList());
        List<TesseractJobDetail> jobDetailList = Collections.synchronizedList(Lists.newArrayList());
        List<String> noExecutorList = Collections.synchronizedList(Lists.newArrayList());
        List<String> noTriggerList = Collections.synchronizedList(Lists.newArrayList());
        //保存job
        tesseractAdminJobDetailDTOList.parallelStream().forEach(tesseractAdminJobDetailDTO -> {
                    @NotBlank String className = tesseractAdminJobDetailDTO.getClassName();
                    @NotBlank String triggerName = tesseractAdminJobDetailDTO.getTriggerName();
                    //检测触发器是否存在
                    QueryWrapper<TesseractTrigger> triggerQueryWrapper = new QueryWrapper<>();
                    triggerQueryWrapper.lambda().eq(TesseractTrigger::getName, triggerName);
                    TesseractTrigger trigger = triggerService.getOne(triggerQueryWrapper);
                    if (trigger == null) {
                        log.warn("触发器{}不存在", triggerName);
                        noTriggerList.add(triggerName);
                        return;
                    }
                    //检测执行器器是否存在
                    @NotNull Integer executorId = trigger.getExecutorId();
                    TesseractExecutor executor = executorService.getById(executorId);
                    if (executor == null) {
                        log.warn("执行器{}不存在", executorId);
                        noExecutorList.add(executorId.toString());
                        return;
                    }

                    bindExecutor(executor, socket);
                    //以防任务重复注册，添加进job
                    QueryWrapper<TesseractJobDetail> jobDetailQueryWrapper = new QueryWrapper<>();
                    jobDetailQueryWrapper.lambda().eq(TesseractJobDetail::getTriggerId, trigger.getId()).eq(TesseractJobDetail::getClassName, className);
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
        tesseractAdminRegistryResDTO.setNotTriggerNameList(noTriggerList);
        tesseractAdminRegistryResDTO.setNoExecutorList(noExecutorList);
        tesseractAdminRegistryResDTO.setRepeatJobList(repeatJobList);
        return tesseractAdminRegistryResDTO;
    }

    /**
     * @param executor
     */
    private void bindExecutor(TesseractExecutor executor, String socket) {
        //防止重复插入
        Integer executorId = executor.getId();
        QueryWrapper<TesseractExecutorDetail> executorDetailQueryWrapper = new QueryWrapper<>();
        executorDetailQueryWrapper.lambda().eq(TesseractExecutorDetail::getSocket, socket);
        TesseractExecutorDetail executorDetail = executorDetailService.getOne(executorDetailQueryWrapper);
        if (executorDetail != null) {
            log.warn("机器{}已注册，将忽略关联", executorDetail);
            return;
        }
        long currentTimeMillis = System.currentTimeMillis();
        executorDetail = new TesseractExecutorDetail();
        executorDetail.setExecutorId(executorId);
        executorDetail.setSocket(socket);
        executorDetail.setUpdateTime(currentTimeMillis);
        executorDetail.setCreateTime(currentTimeMillis);
        executorDetailService.save(executorDetail);
    }
}
