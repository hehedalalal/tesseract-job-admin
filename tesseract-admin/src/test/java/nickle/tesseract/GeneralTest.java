package nickle.tesseract;

import admin.core.scheduler.CronExpression;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;

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
}
