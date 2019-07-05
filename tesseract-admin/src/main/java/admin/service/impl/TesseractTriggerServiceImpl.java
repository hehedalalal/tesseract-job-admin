package admin.service.impl;

import admin.core.scheduler.TesseractTriggerDispatcher;
import admin.entity.TesseractTrigger;
import admin.mapper.TesseractTriggerMapper;
import admin.service.ITesseractLockService;
import admin.service.ITesseractTriggerService;
import admin.util.AdminUtils;
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

import java.util.Arrays;
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
    @Autowired
    private TesseractTriggerDispatcher triggerDispatcher;

    @Transactional
    @Override
    public List<TesseractTrigger> findTriggerWithLock(int batchSize, long time) {
        lockService.lock(TRIGGER_LOCK_NAME);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().le(TesseractTrigger::getNextTriggerTime, time)
                .eq(TesseractTrigger::getStatus, TRGGER_STATUS_STARTING);
        Page<TesseractTrigger> page = new Page<>(1, batchSize);
        IPage<TesseractTrigger> listPage = page(page, queryWrapper);
        List<TesseractTrigger> triggerList = listPage.getRecords();
        if (!CollectionUtils.isEmpty(triggerList)) {
            //更新触发器状态为获取状态
            AdminUtils.updateTriggerStatus(this, triggerList, TRGGER_STATUS_ACCQUIRED);
        }
        return triggerList;
    }

    @Override
    public IPage<TesseractTrigger> listByPage(Integer currentPage, Integer pageSize) {
        Page<TesseractTrigger> page = new Page<>(currentPage, pageSize);
        return page(page);
    }

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
