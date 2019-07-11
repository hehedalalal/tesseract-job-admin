package admin.util;

import admin.pojo.StatisticsLogDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import tesseract.exception.TesseractException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
public class AdminUtils {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 根据StatisticsLogDO，构建返回统计列表
     *
     * @param statisticsLogDOList
     * @param statisticsDays
     * @return
     */
    public static Collection<Integer> buildStatisticsList(List<StatisticsLogDO> statisticsLogDOList, Integer statisticsDays) {
        LinkedHashMap<String, Integer> linkedHashMap = Maps.newLinkedHashMap();
        LocalDate startDate = LocalDate.now().minusDays(6);
        for (int i = 0; i < statisticsDays; i++) {
            linkedHashMap.put(startDate.format(DATE_TIME_FORMATTER), 0);
            startDate = startDate.plusDays(1);
        }
        statisticsLogDOList.forEach(statisticsLogDO -> {
            linkedHashMap.put(statisticsLogDO.getDataStr(), statisticsLogDO.getNum());
        });
        return linkedHashMap.values();
    }

    /**
     * 构建执行条件
     *
     * @param queryWrapper
     * @param obj
     */
    public static void buildCondition(QueryWrapper queryWrapper, Object obj) {
        Class<?> aClass = obj.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        try {
            for (Field field : declaredFields) {
                field.setAccessible(true);
                Object value = field.get(obj);
                String name = field.getName();
                if (!"serialVersionUID".equals(name) && value != null) {
                    //添加进查询条件
                    //String 采用like处理
                    if (value instanceof String && !"".equals(((String) value).trim())) {
                        queryWrapper.like(name, value);
                    } else {
                        queryWrapper.eq(name, value);
                    }

                }
            }
        } catch (Exception e) {
            log.error("buildCondition 发生异常:{}", e.getMessage());
            throw new TesseractException("构建查询条件出错");
        }
    }

}
