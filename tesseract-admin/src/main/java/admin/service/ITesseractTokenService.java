package admin.service;

import admin.entity.TesseractToken;
import admin.pojo.StatisticsLogDO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-06
 */
public interface ITesseractTokenService extends IService<TesseractToken> {

    List<StatisticsLogDO> statisticsActiveUser(long startTime, long endTime);
}
