package admin.service;

import admin.entity.TesseractExecutorDetail;
import com.baomidou.mybatisplus.extension.service.IService;
import tesseract.core.dto.TesseractHeartbeatRequest;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-07
 */
public interface ITesseractExecutorDetailService extends IService<TesseractExecutorDetail> {
    void heartBeat(TesseractHeartbeatRequest heartBeatRequest);

    List<TesseractExecutorDetail> listInvalid();
}
