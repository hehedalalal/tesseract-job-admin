package admin.service;

import admin.entity.TesseractGroup;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-10
 */
public interface ITesseractGroupService extends IService<TesseractGroup> {
    void deleteGroup(Integer groupId);

    void saveOrUpdateGroup(TesseractGroup tesseractGroup);

    IPage<TesseractGroup> listByPage(Integer currentPage, Integer pageSize, TesseractGroup condition, Long startCreateTime, Long endCreateTime);
}
