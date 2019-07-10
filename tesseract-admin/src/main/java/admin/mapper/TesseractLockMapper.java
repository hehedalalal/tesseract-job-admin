package admin.mapper;

import admin.entity.TesseractLock;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import feign.Param;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface TesseractLockMapper extends BaseMapper<TesseractLock> {
    TesseractLock lock(@Param("groupName") String groupName, @Param("lockName") String lockName);
}
