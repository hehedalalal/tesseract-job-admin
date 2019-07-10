package admin.mapper;

import admin.entity.TesseractMenuResource;
import admin.entity.TesseractRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author nickle
 * @since 2019-07-10
 */
public interface TesseractMenuResourceMapper extends BaseMapper<TesseractMenuResource> {

    List<TesseractMenuResource> selectMenusByUserId(@Param("roleIds") List<Integer> roleIds);
}
