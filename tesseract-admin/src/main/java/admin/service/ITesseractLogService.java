package admin.service;

import admin.entity.TesseractLog;
import admin.entity.TesseractUser;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import tesseract.core.dto.TesseractAdminJobNotify;
import tesseract.core.dto.TesseractExecutorResponse;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface ITesseractLogService extends IService<TesseractLog> {
    void notify(TesseractAdminJobNotify tesseractAdminJobNotify);

    IPage<TesseractLog> listByPage(Integer currentPage, Integer pageSize, TesseractLog condition);
}
