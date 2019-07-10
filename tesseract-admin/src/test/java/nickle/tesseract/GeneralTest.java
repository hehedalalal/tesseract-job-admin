package nickle.tesseract;

import admin.core.scheduler.CronExpression;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.LinkedHashMap;

/**
 * 〈〉
 *
 * @author nickel
 * @create 2019/7/7
 * @since 1.0.0
 */

public class GeneralTest {
    @Test
    public void testCronExpression() throws Exception {
        CronExpression cronExpression = new CronExpression("0 0/5 * * * ?");
        System.out.println(cronExpression.getNextValidTimeAfter(new Date()));

    }

    @Test
    public void testLocalDate() {
        LocalDate now = LocalDate.now();
        LocalDate plus = now.plus(1, ChronoUnit.DAYS);
        LocalDate minus = now.minus(6, ChronoUnit.DAYS);
        System.out.println(Period.between(plus, minus).getDays());
        System.out.println(plus.atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli());
        System.out.println(now.minus(7, ChronoUnit.DAYS).atStartOfDay().toInstant(ZoneOffset.of("+8")).toEpochMilli());
    }

    @Test
    public void testStatisticsLocalDate() {
        LinkedHashMap<String, Integer> linkedHashMap = Maps.newLinkedHashMap();
        LocalDate startDate = LocalDate.now().minusDays(7);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            linkedHashMap.put(startDate.format(dateTimeFormatter), 0);
            startDate = startDate.plusDays(1);
        }
        System.out.println(linkedHashMap.keySet());
    }

    @Test
    public void testPass(){
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        //加密"0"
        String encode = bCryptPasswordEncoder.encode("admin");
        System.out.println(encode);
        //结果：$2a$10$/eEV4X7hXPzYGzOLXfCizu6h7iRisp7I116wPA3P9uRcHAKJyY4TK
    }
}
