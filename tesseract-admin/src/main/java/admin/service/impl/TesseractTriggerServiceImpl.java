package admin.service.impl;

import admin.core.scheduler.CronExpression;
import admin.core.scheduler.TesseractScheduleBoot;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractTriggerMapper;
import admin.pojo.PageVO;
import admin.pojo.TriggerVO;
import admin.service.ITesseractLockService;
import admin.service.ITesseractTriggerService;
import admin.util.AdminUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tesseract.exception.TesseractException;

import javax.validation.constraints.NotBlank;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static admin.constant.AdminConstant.*;

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
public class TesseractTriggerServiceImpl extends ServiceImpl<TesseractTriggerMapper, TesseractTrigger> implements ITesseractTriggerService {
    @Autowired
    private ITesseractLockService lockService;

    /**
     * 获取锁并获取到时间点之前的触发器
     *
     * @param batchSize
     * @param time
     * @param timeWindowSize
     * @return
     */
    @Override
    public List<TesseractTrigger> findTriggerWithLock(String groupName, int batchSize, long time, Integer timeWindowSize) {
        lockService.lock(groupName, TRIGGER_LOCK_NAME);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().le(TesseractTrigger::getNextTriggerTime, time + timeWindowSize)
                .eq(TesseractTrigger::getStatus, TRGGER_STATUS_STARTING)
                .eq(TesseractTrigger::getGroupName, groupName)
                .orderByDesc(TesseractTrigger::getNextTriggerTime);
        Page<TesseractTrigger> page = new Page<>(1, batchSize);
        IPage<TesseractTrigger> listPage = page(page, queryWrapper);
        List<TesseractTrigger> triggerList = listPage.getRecords();
        if (!CollectionUtils.isEmpty(triggerList)) {
            triggerList.parallelStream().forEach(trigger -> {
                //构建cron计算器
                CronExpression cronExpression = null;
                try {
                    cronExpression = new CronExpression(trigger.getCron());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long currentTimeMillis = System.currentTimeMillis();
                trigger.setNextTriggerTime(cronExpression.getTimeAfter(new Date()).getTime());
                trigger.setPrevTriggerTime(currentTimeMillis);
            });
            log.info("下一次执行时间:{}", new Date(triggerList.get(0).getNextTriggerTime()));
            this.updateBatchById(triggerList);
        }
        return triggerList;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateTrigger(TesseractTrigger tesseractTrigger) throws Exception {
        CronExpression cronExpression = new CronExpression(tesseractTrigger.getCron());
        long currentTimeMillis = System.currentTimeMillis();
        Integer triggerId = tesseractTrigger.getId();
        //更新
        if (triggerId != null) {
            TesseractTrigger trigger = getById(triggerId);
            @NotBlank String oldCron = trigger.getCron();
            //重新计算下一次调度时间
            if (!tesseractTrigger.getCron().equals(oldCron)) {
                tesseractTrigger.setNextTriggerTime(cronExpression.getTimeAfter(new Date()).getTime());
            }
            updateById(tesseractTrigger);
            return;
        }
        tesseractTrigger.setPrevTriggerTime(0L);
        tesseractTrigger.setNextTriggerTime(cronExpression.getTimeAfter(new Date()).getTime());
        tesseractTrigger.setCreateTime(currentTimeMillis);
        tesseractTrigger.setStatus(TRGGER_STATUS_STOPING);
        tesseractTrigger.setUpdateTime(currentTimeMillis);
        this.save(tesseractTrigger);
    }

    @Transactional
    @Override
    public TriggerVO listByPage(Integer currentPage, Integer pageSize, TesseractTrigger condition,
                                Long startCreateTime,
                                Long endCreateTime) {
        Page<TesseractTrigger> page = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TesseractTrigger> lambda = queryWrapper.lambda();
        //日期
        if (startCreateTime != null) {
            lambda.ge(TesseractTrigger::getCreateTime, startCreateTime);
        }

        if (endCreateTime != null) {
            lambda.le(TesseractTrigger::getCreateTime, endCreateTime);
        }
        AdminUtils.buildCondition(queryWrapper, condition);
        IPage<TesseractTrigger> pageInfo = page(page, queryWrapper);
        TriggerVO triggerVO = new TriggerVO();
        PageVO pageVO = new PageVO();
        pageVO.setCurrentPage(pageInfo.getCurrent());
        pageVO.setPageSize(pageInfo.getSize());
        pageVO.setTotal(pageInfo.getTotal());
        triggerVO.setPageInfo(pageVO);
        List<TesseractTrigger> triggerList = pageInfo.getRecords();
        triggerVO.setTriggerList(triggerList);
        return triggerVO;
    }

    @Override
    public void executeTrigger(String groupName, Integer triggerId) {
        TesseractScheduleBoot.executeTrigger(groupName, Arrays.asList(getTriggerById(triggerId)));
    }

    @Override
    public void startTrigger(Integer triggerId) throws ParseException {
        TesseractTrigger trigger = getTriggerById(triggerId);
        CronExpression cronExpression = new CronExpression(trigger.getCron());
        trigger.setNextTriggerTime(cronExpression.getTimeAfter(new Date()).getTime());
        trigger.setStatus(TRGGER_STATUS_STARTING);
        updateById(trigger);
    }

    @Override
    public void stopTrigger(Integer triggerId) {
        TesseractTrigger trigger = getTriggerById(triggerId);
        trigger.setStatus(TRGGER_STATUS_STOPING);
        updateById(trigger);
    }

    @Override
    public void deleteTrigger(Integer triggerId) {
        deleteTrigger(triggerId);
    }

    private TesseractTrigger getTriggerById(Integer triggerId) {
        TesseractTrigger trigger = getById(triggerId);
        if (trigger == null) {
            log.error("找不到对应触发器:{}", triggerId);
            throw new TesseractException("找不到对应触发器:" + triggerId);
        }
        return trigger;
    }
}
