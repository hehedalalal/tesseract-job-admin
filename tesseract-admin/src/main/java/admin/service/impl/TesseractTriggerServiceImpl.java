package admin.service.impl;

import admin.core.scheduler.CronExpression;
import admin.core.scheduler.TesseractTriggerDispatcher;
import admin.entity.TesseractFiredTrigger;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractTriggerMapper;
import admin.pojo.PageVO;
import admin.pojo.TriggerVO;
import admin.service.ITesseractFiredTriggerService;
import admin.service.ITesseractLockService;
import admin.service.ITesseractTriggerService;
import admin.util.AdminUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tesseract.exception.TesseractException;

import java.text.ParseException;
import java.util.*;

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
    @Autowired
    private TesseractTriggerDispatcher triggerDispatcher;


    @Transactional
    @Override
    public List<TesseractTrigger> findTriggerWithLock(int batchSize, long time, Integer timeWindowSize) {
        lockService.lock(TRIGGER_LOCK_NAME);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().le(TesseractTrigger::getNextTriggerTime, time + timeWindowSize)
                .eq(TesseractTrigger::getStatus, TRGGER_STATUS_STARTING).orderByDesc(TesseractTrigger::getNextTriggerTime);
        Page<TesseractTrigger> page = new Page<>(1, batchSize);
        IPage<TesseractTrigger> listPage = page(page, queryWrapper);
        List<TesseractTrigger> triggerList = listPage.getRecords();
        if (!CollectionUtils.isEmpty(triggerList)) {
            triggerList.parallelStream().forEach(trigger -> {
                CronExpression cronExpression = null;
                try {
                    cronExpression = new CronExpression(trigger.getCron());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long currentTimeMillis = System.currentTimeMillis();
                Date date = new Date();
                date.setTime(currentTimeMillis);
                trigger.setNextTriggerTime(cronExpression.getTimeAfter(date).getTime());
                trigger.setPrevTriggerTime(currentTimeMillis);
            });
            this.updateBatchById(triggerList);
        }
        return triggerList;
    }


    @Transactional
    @Override
    public void saveTrigger(TesseractTrigger tesseractTrigger) throws Exception {
        CronExpression cronExpression = new CronExpression(tesseractTrigger.getCron());
        long currentTimeMillis = System.currentTimeMillis();
        Date date = new Date();
        date.setTime(currentTimeMillis);
        tesseractTrigger.setPrevTriggerTime(0L);
        tesseractTrigger.setNextTriggerTime(cronExpression.getTimeAfter(date).getTime());
        tesseractTrigger.setCreateTime(currentTimeMillis);
        tesseractTrigger.setStatus(TRGGER_STATUS_STOPING);
        tesseractTrigger.setUpdateTime(currentTimeMillis);
        this.save(tesseractTrigger);
    }

    @Transactional
    @Override
    public TriggerVO listByPage(Integer currentPage, Integer pageSize, TesseractTrigger condition) {
        Page<TesseractTrigger> page = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<TesseractTrigger> lambda = queryWrapper.lambda();
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

    @Transactional
    @Override
    public void executeTrigger(Integer triggerId) {
        triggerDispatcher.dispatchTrigger(Arrays.asList(getTriggerById(triggerId)), true);
    }

    @Override
    public void startTrigger(Integer triggerId) {
        TesseractTrigger trigger = getTriggerById(triggerId);
        trigger.setStatus(TRGGER_STATUS_STARTING);
        updateById(trigger);
    }

    @Override
    public void stopTrigger(Integer triggerId) {
        TesseractTrigger trigger = getTriggerById(triggerId);
        trigger.setStatus(TRGGER_STATUS_STOPING);
        updateById(trigger);
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
