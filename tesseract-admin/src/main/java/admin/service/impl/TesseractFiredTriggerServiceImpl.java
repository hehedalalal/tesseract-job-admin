package admin.service.impl;

import admin.entity.TesseractFiredTrigger;
import admin.entity.TesseractLog;
import admin.mapper.TesseractFiredTriggerMapper;
import admin.service.ITesseractFiredTriggerService;
import admin.service.ITesseractLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-06
 */
@Service
public class TesseractFiredTriggerServiceImpl extends ServiceImpl<TesseractFiredTriggerMapper, TesseractFiredTrigger> implements ITesseractFiredTriggerService {
    @Autowired
    private ITesseractLogService logService;

    @Transactional
    @Override
    public void removeFiredTriggerAndUpdateLog(Integer triggerId, Integer executorDetailId, TesseractLog tesseractLog) {
        QueryWrapper<TesseractFiredTrigger> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TesseractFiredTrigger::getTriggerId, triggerId).eq(TesseractFiredTrigger::getExecutorDetailId, executorDetailId);
        this.remove(queryWrapper);
        logService.updateById(tesseractLog);
    }
}
