package admin.service;

import admin.entity.TesseractLock;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
public interface ITesseractLockService extends IService<TesseractLock> {
    TesseractLock lock(String lockName, String groupName);
}
