package admin.service.impl;

import admin.entity.TesseractLog;
import admin.mapper.TesseractLogMapper;
import admin.pojo.StatisticsLogDO;
import admin.service.ITesseractFiredTriggerService;
import admin.service.ITesseractLogService;
import admin.util.AdminUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import tesseract.core.dto.TesseractAdminJobNotify;
import tesseract.exception.TesseractException;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static admin.constant.AdminConstant.LOG_FAIL;
import static admin.constant.AdminConstant.LOG_SUCCESS;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author nickle
 * @since 2019-07-03
 */

@Slf4j
@Service
public class TesseractLogServiceImpl extends ServiceImpl<TesseractLogMapper, TesseractLog> implements ITesseractLogService {
    @Autowired
    private ITesseractFiredTriggerService firedTriggerService;
    private int statisticsDays = 7;
    private Map<String, Integer> dataMap = Maps.newHashMap();

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notify(TesseractAdminJobNotify tesseractAdminJobNotify) {
        Long logId = tesseractAdminJobNotify.getLogId();
        String exception = tesseractAdminJobNotify.getException();
        Integer triggerId = tesseractAdminJobNotify.getTriggerId();
        @NotNull Integer executorId = tesseractAdminJobNotify.getExecutorId();
        TesseractLog tesseractLog = this.getById(logId);
        if (tesseractLog == null) {
            log.error("获取日志为空:{}", tesseractAdminJobNotify);
            throw new TesseractException("获取日志为空" + tesseractAdminJobNotify);
        }
        if (!StringUtils.isEmpty(exception)) {
            tesseractLog.setStatus(LOG_FAIL);
            tesseractLog.setMsg(exception);
        } else {
            tesseractLog.setStatus(LOG_SUCCESS);
            tesseractLog.setMsg("执行成功");
        }
        tesseractLog.setEndTime(System.currentTimeMillis());
        firedTriggerService.removeFiredTriggerAndUpdateLog(triggerId, executorId, tesseractLog);
    }

    @Override
    public IPage<TesseractLog> listByPage(Integer currentPage, Integer pageSize, TesseractLog condition) {
        Page<TesseractLog> page = new Page<>(currentPage, pageSize);
        QueryWrapper<TesseractLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(TesseractLog::getCreateTime);
        LambdaQueryWrapper<TesseractLog> lambda = queryWrapper.lambda();
        return page(page, queryWrapper);
    }

    @Override
    public Map<String, Collection<Integer>> statisticsLogLine() {
        LocalDate now = LocalDate.now();
        long startTime = now.minus(6, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long endTime = now.plus(1, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        Date startDate = new Date();
        startDate.setTime(startTime);
        Date endDate = new Date();
        startDate.setTime(endTime);
        log.info("startTime:{},endTime:{}", startDate, endDate);
        List<StatisticsLogDO> failStatisticsLogDOList = this.getBaseMapper().statisticsFailLog(startTime, endTime);
        List<StatisticsLogDO> successStatisticsLogDOList = this.getBaseMapper().statisticsSuccessLogLine(startTime, endTime);
        Map<String, Collection<Integer>> map = Maps.newHashMap();
        Collection<Integer> failCountList = AdminUtils.buildStatisticsList(failStatisticsLogDOList, statisticsDays);
        Collection<Integer> successCountList = AdminUtils.buildStatisticsList(successStatisticsLogDOList, statisticsDays);
        map.put("success", successCountList);
        map.put("fail", failCountList);
        return map;
    }

    @Override
    public List<Map<String, Object>> statisticsLogPie() {
        List<Map<String, Object>> list = Lists.newArrayList();
        List<StatisticsLogDO> statisticsLogDOList = this.getBaseMapper().statisticsSuccessLogPie();
        statisticsLogDOList.forEach(statisticsLogDO -> {
            HashMap<String, Object> hashMap = Maps.newHashMap();
            hashMap.put("name", statisticsLogDO.getDataStr());
            hashMap.put("value", statisticsLogDO.getNum());
            list.add(hashMap);
        });
        return list;
    }
}
