package admin.util;

import admin.pojo.StatisticsLogDO;
import com.google.common.collect.Maps;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;


public class AdminUtils {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 根据返回的StatisticsLogDO，构建返回统计列表
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
}
