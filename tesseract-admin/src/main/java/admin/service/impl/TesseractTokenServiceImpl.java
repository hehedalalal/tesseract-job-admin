package admin.service.impl;

import admin.entity.TesseractToken;
import admin.mapper.TesseractTokenMapper;
import admin.pojo.StatisticsLogDO;
import admin.service.ITesseractTokenService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-06
 */
@Service
public class TesseractTokenServiceImpl extends ServiceImpl<TesseractTokenMapper, TesseractToken> implements ITesseractTokenService {

    @Override
    public List<StatisticsLogDO> statisticsActiveUser(long startTime, long endTime) {
        return this.getBaseMapper().statisticsActiveUser(startTime, endTime);
    }
}
