package admin.service;

import admin.entity.TesseractExecutor;
import com.baomidou.mybatisplus.extension.service.IService;
import tesseract.core.dto.TesseractAdminRegistryRequest;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface ITesseractExecutorService extends IService<TesseractExecutor> {
    void registry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception;
}
