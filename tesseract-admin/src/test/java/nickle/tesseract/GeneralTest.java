package nickle.tesseract;

import admin.core.scheduler.CronExpression;
import org.junit.Test;

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
}
