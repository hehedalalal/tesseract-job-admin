package admin.service.impl;

import admin.entity.TesseractExecutorDetail;
import admin.mapper.TesseractExecutorDetailMapper;
import admin.service.ITesseractExecutorDetailService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tesseract.core.dto.TesseractHeartbeatRequest;
import tesseract.exception.TesseractException;

import javax.validation.constraints.NotNull;
import java.util.List;

import static tesseract.core.constant.CommonConstant.EXECUTOR_DETAIL_NOT_FIND;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-07
 */
@Service
@Slf4j
public class TesseractExecutorDetailServiceImpl extends ServiceImpl<TesseractExecutorDetailMapper, TesseractExecutorDetail> implements ITesseractExecutorDetailService {
    private Integer invalidTime = 15 * 1000;

    @Override
    public void heartBeat(TesseractHeartbeatRequest heartBeatRequest) {
        @NotNull String socket = heartBeatRequest.getSocket();
        QueryWrapper<TesseractExecutorDetail> detailQueryWrapper = new QueryWrapper<>();
        detailQueryWrapper.lambda().eq(TesseractExecutorDetail::getSocket, socket);
        TesseractExecutorDetail executorDetail = getOne(detailQueryWrapper);
        if (executorDetail == null) {
            log.warn("机器:{}已失效", socket);
            throw new TesseractException(EXECUTOR_DETAIL_NOT_FIND, "机器已失效");
        }
        executorDetail.setUpdateTime(System.currentTimeMillis());
        updateById(executorDetail);
    }

    @Override
    public List<TesseractExecutorDetail> listInvalid() {
        long currentTimeMillis = System.currentTimeMillis();
        QueryWrapper<TesseractExecutorDetail> detailQueryWrapper = new QueryWrapper<>();
        detailQueryWrapper.lambda().le(TesseractExecutorDetail::getUpdateTime, currentTimeMillis - invalidTime);
        return list(detailQueryWrapper);
    }
}
