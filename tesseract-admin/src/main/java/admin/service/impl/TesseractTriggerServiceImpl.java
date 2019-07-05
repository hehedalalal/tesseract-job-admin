package admin.service.impl;

import admin.entity.TesseractTrigger;
import admin.mapper.TesseractTriggerMapper;
import admin.service.ITesseractLockService;
import admin.service.ITesseractTriggerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static admin.constant.AdminConstant.TRGGER_STATUS_START;
import static admin.constant.AdminConstant.TRIGGER_LOCK_NAME;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@Service
public class TesseractTriggerServiceImpl extends ServiceImpl<TesseractTriggerMapper, TesseractTrigger> implements ITesseractTriggerService {
    @Autowired
    private ITesseractLockService lockService;

    @Transactional
    @Override
    public List<TesseractTrigger> findTriggerWithLock(int batchSize, long time) {
        lockService.lock(TRIGGER_LOCK_NAME);
        QueryWrapper<TesseractTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().le(TesseractTrigger::getNextTriggerTime, time)
                .eq(TesseractTrigger::getStatus, TRGGER_STATUS_START);
        Page<TesseractTrigger> page = new Page<>(1, batchSize);
        IPage<TesseractTrigger> listPage = page(page, queryWrapper);
        return listPage.getRecords();
    }

    @Override
    public IPage<TesseractTrigger> listByPage(Integer currentPage, Integer pageSize) {
        Page<TesseractTrigger> page = new Page<>(currentPage, pageSize);
        return page(page);
    }
}
