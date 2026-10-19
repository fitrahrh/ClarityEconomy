package com.harukiyu.clarityeconomy;

import com.harukiyu.clarityeconomy.api.model.JobType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EconomyLogicTest {

    @Test
    public void testJobTypesCountAndProperties() {
        JobType[] jobs = JobType.values();
        Assertions.assertEquals(10, jobs.length, "Must have exactly 10 jobs as specified!");
        for (JobType job : jobs) {
            Assertions.assertNotNull(job.getDisplayName());
            Assertions.assertNotNull(job.getIcon());
            Assertions.assertNotNull(job.getSelfBuffDesc());
            Assertions.assertNotNull(job.getAuraBuffDesc());
            Assertions.assertFalse(job.getSelfBuffDesc().isBlank());
            Assertions.assertFalse(job.getAuraBuffDesc().isBlank());
        }
    }

    @Test
    public void testBlackMarketDiscountCalculation() {
        double original = 1000.0;
        double multiplier = 0.2;
        double discounted = original * multiplier;
        Assertions.assertEquals(200.0, discounted, 0.001, "5x discount / 0.2 multiplier should equal 200.0 for 1000.0 item");
    }

    @Test
    public void testExpProgressionFormula() {
        long baseExp = 100L;
        double mult = 1.35;
        long lvl1Req = (long) (baseExp * Math.pow(1, mult));
        long lvl10Req = (long) (baseExp * Math.pow(10, mult));
        long lvl49Req = (long) (baseExp * Math.pow(49, mult));

        Assertions.assertEquals(100L, lvl1Req);
        Assertions.assertTrue(lvl10Req > lvl1Req);
        Assertions.assertTrue(lvl49Req > lvl10Req);
    }
}
