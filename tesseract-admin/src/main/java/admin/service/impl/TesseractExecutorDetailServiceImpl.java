package admin.service.impl;

import admin.entity.TesseractExecutorDetail;
import admin.entity.TesseractFiredTrigger;
import admin.entity.TesseractLog;
import admin.mapper.TesseractExecutorDetailMapper;
import admin.service.ITesseractExecutorDetailService;
import admin.service.ITesseractFiredTriggerService;
import admin.service.ITesseractLogService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tesseract.core.dto.TesseractHeartbeatRequest;
import tesseract.exception.TesseractException;

import javax.validation.constraints.NotNull;
import java.util.List;

import static admin.constant.AdminConstant.LOG_NO_CONFIRM;
import static tesseract.core.constant.CommonConstant.EXECUTOR_DETAIL_NOT_FIND;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-07
 */
@Service
@Slf4j
public class TesseractExecutorDetailServiceImpl extends ServiceImpl<TesseractExecutorDetailMapper, TesseractExecutorDetail> implements ITesseractExecutorDetailService {
    private Integer invalidTime = 15 * 1000;
    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;
    @Autowired
    private ITesseractLogService logService;

    @Override
    public void heartBeat(TesseractHeartbeatRequest heartBeatRequest) {
        @NotNull String socket = heartBeatRequest.getSocket();
        QueryWrapper<TesseractExecutorDetail> detailQueryWrapper = new QueryWrapper<>();
        detailQueryWrapper.lambda().eq(TesseractExecutorDetail::getSocket, socket);
        TesseractExecutorDetail executorDetail = getOne(detailQueryWrapper);
        if (executorDetail == null) {
            log.warn("机器:{}已失效", socket);
            checkFiredTrigger(executorDetail);
            throw new TesseractException(EXECUTOR_DETAIL_NOT_FIND, "机器已失效");
        }
        executorDetail.setUpdateTime(System.currentTimeMillis());
        updateById(executorDetail);
    }

    @Override
    public List<TesseractExecutorDetail> listInvalid() {
        long currentTimeMillis = System.currentTimeMillis();
        QueryWrapper<TesseractExecutorDetail> detailQueryWrapper = new QueryWrapper<>();
        detailQueryWrapper.lambda().le(TesseractExecutorDetail::getUpdateTime, currentTimeMillis - invalidTime);
        return list(detailQueryWrapper);
    }

    /**
     * 检查fired表，如果有则报警并且更改日志状态
     *
     * @param executorDetail
     */
    private void checkFiredTrigger(TesseractExecutorDetail executorDetail) {
        QueryWrapper<TesseractFiredTrigger> firedTriggerQueryWrapper = new QueryWrapper<>();
        firedTriggerQueryWrapper.lambda().eq(TesseractFiredTrigger::getExecutorDetailId, executorDetail.getId());
        List<TesseractFiredTrigger> firedTriggerList = firedTriggerService.list(firedTriggerQueryWrapper);
        if (!CollectionUtils.isEmpty(firedTriggerList)) {
            firedTriggerList.parallelStream().forEach(firedTrigger -> {
                // TODO: 2019/7/8  需要报警处理
                log.warn("滞留触发器列表:{}", firedTriggerList);
                Long logId = firedTrigger.getLogId();
                //更改日志状态
                TesseractLog log = new TesseractLog();
                log.setStatus(LOG_NO_CONFIRM);
                UpdateWrapper<TesseractLog> logUpdateWrapper = new UpdateWrapper<>();
                logUpdateWrapper.lambda().eq(TesseractLog::getId, logId);
                logService.update(log, logUpdateWrapper);
            });
        }
    }
}
