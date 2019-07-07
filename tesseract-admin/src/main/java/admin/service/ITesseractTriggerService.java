package admin.service;

import admin.entity.TesseractTrigger;
import admin.pojo.TriggerVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface ITesseractTriggerService extends IService<TesseractTrigger> {
    List<TesseractTrigger> findTriggerWithLock(int batchSize, long time, Integer timeWindowSize);

    TriggerVO listByPage(Integer currentPage, Integer pageSize, TesseractTrigger condition);

    void executeTrigger(Integer triggerId);

    void startTrigger(Integer triggerId);

    void stopTrigger(Integer triggerId);

    void saveTrigger(TesseractTrigger tesseractTrigger) throws Exception;


}
