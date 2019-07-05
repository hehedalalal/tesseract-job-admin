package admin.service.impl;

import admin.entity.TesseractLock;
import admin.mapper.TesseractLockMapper;
import admin.service.ITesseractLockService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@Service
public class TesseractLockServiceImpl extends ServiceImpl<TesseractLockMapper, TesseractLock> implements ITesseractLockService {
    @Transactional
    @Override
    public TesseractLock lock(String lockName) {
        return this.baseMapper.lock(lockName);
    }
}
