package org.routineslim.domain.subscription.entity;

/** 결제 주기. cycle_interval 과 함께 쓴다 (2주마다 = WEEKLY × 2) */
public enum BillingCycle {
    WEEKLY,
    MONTHLY,
    YEARLY
}
