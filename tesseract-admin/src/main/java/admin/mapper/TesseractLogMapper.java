package admin.mapper;

import admin.entity.TesseractLog;
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
 * @since 2019-07-03
 */
public interface TesseractLogMapper extends BaseMapper<TesseractLog> {

    List<StatisticsLogDO> statisticsFailLog(@Param("startTime") long startTime, @Param("endTime") long endTime);

    List<StatisticsLogDO> statisticsSuccessLog(@Param("startTime") long startTime, @Param("endTime") long endTime);
}
