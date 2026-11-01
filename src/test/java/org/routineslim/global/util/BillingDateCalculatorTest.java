package org.routineslim.global.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.routineslim.domain.subscription.entity.BillingCycle;

class BillingDateCalculatorTest {

    @Test
    void 매월_31일_결제는_짧은_달에_말일로_당겨졌다가_다시_31일로_돌아온다() {
        LocalDate start = LocalDate.of(2026, 1, 31);
        assertEquals(LocalDate.of(2026, 2, 28), nth(start, BillingCycle.MONTHLY, 1, 1));
        assertEquals(LocalDate.of(2026, 3, 31), nth(start, BillingCycle.MONTHLY, 1, 2));
        assertEquals(LocalDate.of(2026, 4, 30), nth(start, BillingCycle.MONTHLY, 1, 3));
    }

    @Test
    void 연간_2월_29일_결제는_평년에_28일로_당겨진다() {
        LocalDate start = LocalDate.of(2028, 2, 29);
        assertEquals(LocalDate.of(2029, 2, 28), nth(start, BillingCycle.YEARLY, 1, 1));
        assertEquals(LocalDate.of(2032, 2, 29), nth(start, BillingCycle.YEARLY, 1, 4));
    }

    @Test
    void 주기_배수를_반영한다() {
        LocalDate start = LocalDate.of(2026, 10, 1);
        assertEquals(LocalDate.of(2026, 10, 15), nth(start, BillingCycle.WEEKLY, 2, 1));
        assertEquals(LocalDate.of(2027, 1, 1), nth(start, BillingCycle.MONTHLY, 3, 1));
    }

    @Test
    void 오늘_이후_첫_결제일() {
        LocalDate start = LocalDate.of(2026, 1, 31);
        // 오늘이 결제일이면 오늘
        assertEquals(LocalDate.of(2026, 10, 31), firstOnOrAfter(start, LocalDate.of(2026, 10, 31)));
        assertEquals(LocalDate.of(2026, 11, 30), firstOnOrAfter(start, LocalDate.of(2026, 11, 1)));
        // 시작일이 미래면 시작일
        assertEquals(LocalDate.of(2027, 3, 5),
                BillingDateCalculator.firstOnOrAfter(LocalDate.of(2027, 3, 5), BillingCycle.MONTHLY, 1, 5,
                        LocalDate.of(2026, 10, 25)));
    }

    @Test
    void 결제일_다음_결제일() {
        LocalDate start = LocalDate.of(2026, 1, 31);
        assertEquals(LocalDate.of(2026, 3, 31),
                BillingDateCalculator.firstAfter(start, BillingCycle.MONTHLY, 1, 31, LocalDate.of(2026, 2, 28)));
    }

    @Test
    void 월_금액_환산() {
        assertEquals(17000, BillingDateCalculator.monthlyAmount(17000, BillingCycle.MONTHLY, 1));
        assertEquals(9908, BillingDateCalculator.monthlyAmount(118900, BillingCycle.YEARLY, 1));
        assertEquals(43333, BillingDateCalculator.monthlyAmount(10000, BillingCycle.WEEKLY, 1));
        assertEquals(5000, BillingDateCalculator.monthlyAmount(10000, BillingCycle.MONTHLY, 2));
    }

    private static LocalDate nth(LocalDate start, BillingCycle cycle, int interval, long n) {
        return BillingDateCalculator.nthBillingDate(start, cycle, interval, start.getDayOfMonth(), n);
    }

    private static LocalDate firstOnOrAfter(LocalDate start, LocalDate date) {
        return BillingDateCalculator.firstOnOrAfter(start, BillingCycle.MONTHLY, 1, start.getDayOfMonth(), date);
    }
}
