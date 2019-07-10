package admin.service.impl;

import admin.entity.TesseractLock;
import admin.mapper.TesseractLockMapper;
import admin.service.ITesseractLockService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ConcurrentHashMap;

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
    private ConcurrentHashMap<String, Boolean> checkMap = new ConcurrentHashMap<>();

    @Transactional
    @Override
    public TesseractLock lock(String lockName, String groupName) {
        String key = lockName + groupName;
        //检测表内是否存在锁字段
        if (checkMap.get(key) == null) {
            QueryWrapper<TesseractLock> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TesseractLock::getGroupName, groupName).eq(TesseractLock::getName, lockName);
            TesseractLock tesseractLock = getOne(queryWrapper);
            //锁不存在上锁，锁存在添加标识
            if (tesseractLock == null) {
                tesseractLock = new TesseractLock();
                tesseractLock.setName(lockName);
                tesseractLock.setGroupName(groupName);
                this.save(tesseractLock);
                checkMap.put(key, true);
            } else {
                checkMap.put(key, true);
            }
        }
        return this.baseMapper.lock(groupName, lockName);
    }
}
