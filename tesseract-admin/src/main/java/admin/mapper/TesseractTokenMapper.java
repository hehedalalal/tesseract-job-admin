package admin.mapper;

import admin.entity.TesseractToken;
import admin.pojo.StatisticsLogDO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author nickle
 * @since 2019-07-06
 */
public interface TesseractTokenMapper extends BaseMapper<TesseractToken> {
    List<StatisticsLogDO> statisticsActiveUser(@Param("startTime") long startTime, @Param("endTime") long endTime);
}
