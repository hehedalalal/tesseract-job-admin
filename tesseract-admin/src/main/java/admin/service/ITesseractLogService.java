package admin.service;

import admin.entity.TesseractLog;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import tesseract.core.dto.TesseractAdminJobNotify;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    Map<String, Collection<Integer>> statisticsLogLine();

    List<Map<String, Object>> statisticsLogPie();
}
