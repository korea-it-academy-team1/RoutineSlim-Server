package org.routineslim.global.util;

import java.time.LocalDate;
import java.time.YearMonth;

import org.routineslim.domain.subscription.entity.BillingCycle;

/**
 * 결제일 계산.
 * billingDay(원래 결제일)를 기준으로 계산해서, 31일 결제가 2월에 28일로 당겨져도 3월에는 다시 31일이 된다.
 */
public final class BillingDateCalculator {

    private BillingDateCalculator() {
    }

    /** 시작일부터 n번째 결제일 (n = 0이면 시작일) */
    public static LocalDate nthBillingDate(LocalDate startDate, BillingCycle cycle, int cycleInterval,
                                           int billingDay, long n) {
        long steps = n * cycleInterval;
        return switch (cycle) {
            case WEEKLY -> startDate.plusWeeks(steps);
            case MONTHLY -> atDayClamped(YearMonth.from(startDate).plusMonths(steps), billingDay);
            case YEARLY -> atDayClamped(YearMonth.from(startDate).plusYears(steps), billingDay);
        };
    }

    /** date 당일이거나 그 이후인 첫 결제일 */
    public static LocalDate firstOnOrAfter(LocalDate startDate, BillingCycle cycle, int cycleInterval,
                                           int billingDay, LocalDate date) {
        long n = 0;
        LocalDate billingDate = nthBillingDate(startDate, cycle, cycleInterval, billingDay, n);
        while (billingDate.isBefore(date)) {
            billingDate = nthBillingDate(startDate, cycle, cycleInterval, billingDay, ++n);
        }
        return billingDate;
    }

    /** date 다음 날 이후인 첫 결제일 (결제일이 지난 뒤 다음 결제일을 구할 때) */
    public static LocalDate firstAfter(LocalDate startDate, BillingCycle cycle, int cycleInterval,
                                       int billingDay, LocalDate date) {
        return firstOnOrAfter(startDate, cycle, cycleInterval, billingDay, date.plusDays(1));
    }

    /** 1회 결제 금액을 월 금액으로 환산 (원 단위 반올림) */
    public static int monthlyAmount(int amount, BillingCycle cycle, int cycleInterval) {
        double monthly = switch (cycle) {
            case WEEKLY -> amount * 52.0 / 12.0 / cycleInterval;
            case MONTHLY -> (double) amount / cycleInterval;
            case YEARLY -> amount / 12.0 / cycleInterval;
        };
        return (int) Math.round(monthly);
    }

    private static LocalDate atDayClamped(YearMonth yearMonth, int day) {
        return yearMonth.atDay(Math.min(day, yearMonth.lengthOfMonth()));
    }
}
