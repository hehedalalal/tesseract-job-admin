package admin.service.impl;

import admin.core.scheduler.CronExpression;
import admin.core.scheduler.TesseractTriggerDispatcher;
import admin.entity.TesseractFiredTrigger;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractTriggerMapper;
import admin.service.ITesseractFiredTriggerService;
import admin.service.ITesseractLockService;
import admin.service.ITesseractTriggerService;
import admin.util.AdminUtils;
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
    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;


    @Transactional
    @Override
    public List<TesseractTrigger> findTriggerWithLock(int batchSize, long time, Integer timeWindowSize) {
        lockService.lock(TRIGGER_LOCK_NAME);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().le(TesseractTrigger::getNextTriggerTime, time+timeWindowSize)
                .eq(TesseractTrigger::getStatus, TRGGER_STATUS_STARTING).orderByDesc(TesseractTrigger::getNextTriggerTime);
        Page<TesseractTrigger> page = new Page<>(1, batchSize);
        IPage<TesseractTrigger> listPage = page(page, queryWrapper);
        List<TesseractTrigger> triggerList = listPage.getRecords();
        if (!CollectionUtils.isEmpty(triggerList)) {
            //更新触发器状态为获取状态
            updateTriggerStatus(triggerList, TRGGER_STATUS_ACCQUIRED);
        }
        return triggerList;
    }

    @Transactional
    @Override
    public void updateTriggerStatus(List<TesseractTrigger> triggerList, Integer status) {
        triggerList.parallelStream().forEach(trigger -> trigger.setStatus(status));
        this.updateBatchById(triggerList);
    }

    @Transactional
    @Override
    public void updateTriggerStatusAndCalculate(List<TesseractTrigger> triggerList, Integer status) {
        List<Integer> triggerIdList = Collections.synchronizedList(Lists.newArrayList());
        triggerList.parallelStream().forEach(trigger -> {
            try {
                CronExpression cronExpression = new CronExpression(trigger.getCron());
                Date date = new Date();
                date.setTime(trigger.getPrevTriggerTime());
                trigger.setNextTriggerTime(cronExpression.getTimeAfter(date).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            triggerIdList.add(trigger.getId());
            trigger.setStatus(status);
        });
        this.updateBatchById(triggerList);
    }

    @Transactional
    @Override
    public void updateTriggerStatusAndDeleteFiredTrigger(List<TesseractTrigger> triggerList, Integer status) {
        List<Integer> triggerIdList = Collections.synchronizedList(Lists.newArrayList());
        triggerList.parallelStream().forEach(trigger -> {
            triggerIdList.add(trigger.getId());
            trigger.setStatus(status);
        });
        this.updateBatchById(triggerList);
        firedTriggerService.removeByIds(triggerIdList);
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
    public IPage<TesseractTrigger> listByPage(Integer currentPage, Integer pageSize) {
        Page<TesseractTrigger> page = new Page<>(currentPage, pageSize);
        return page(page);
    }

    @Transactional
    @Override
    public void executeTrigger(Integer triggerId) {
        TesseractTrigger byId = getById(triggerId);
        if (byId == null) {
            log.error("找不到对应触发器:{}", triggerId);
            throw new TesseractException("找不到对应触发器:" + triggerId);
        }
        triggerDispatcher.dispatchTrigger(Arrays.asList(byId), true);
    }
}
