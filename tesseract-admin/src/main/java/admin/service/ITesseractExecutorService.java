package admin.service;

import admin.entity.TesseractExecutor;
import admin.pojo.ExecutorVO;
import com.baomidou.mybatisplus.extension.service.IService;
import tesseract.core.dto.TesseractAdminRegistryRequest;
import tesseract.core.dto.TesseractAdminRegistryResDTO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface ITesseractExecutorService extends IService<TesseractExecutor> {
    TesseractAdminRegistryResDTO registry(TesseractAdminRegistryRequest tesseractAdminRegistryRequest) throws Exception;

    ExecutorVO listByPage(Long currentPage, Long pageSize, TesseractExecutor condition);

    void saveExecutor(TesseractExecutor tesseractExecutor);
}
