package admin.service;

import admin.entity.TesseractFiredTrigger;
import admin.entity.TesseractLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-06
 */
public interface ITesseractFiredTriggerService extends IService<TesseractFiredTrigger> {
    void removeFiredTriggerAndUpdateLog(Integer triggerId, Integer executorId, TesseractLog log);
}
