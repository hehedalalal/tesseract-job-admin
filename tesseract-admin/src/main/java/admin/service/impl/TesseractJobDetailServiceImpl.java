package admin.service.impl;

import admin.entity.TesseractJobDetail;
import admin.mapper.TesseractJobDetailMapper;
import admin.service.ITesseractJobDetailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */
@Service
@Slf4j
public class TesseractJobDetailServiceImpl extends ServiceImpl<TesseractJobDetailMapper, TesseractJobDetail> implements ITesseractJobDetailService {
//    @Autowired
//    private ITesseractLockService lockService;
//
//    @Transactional
//    @Override
//    public void saveBatchWithLock(List<TesseractJobDetail> jobDetailList) {
//        lockService.lock(JOB_LOCK_NAME);
//        try {
//            this.saveBatch(jobDetailList);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }
//    }
}
