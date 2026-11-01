package org.routineslim.domain.subscription.entity;

/** 구독 상태. 해지는 삭제가 아니라 CANCELED 로 남긴다 */
public enum SubscriptionStatus {
    /** 무료 체험 중. 첫 결제일(start_date)이 지나면 ACTIVE 로 바뀐다 */
    TRIAL,
    ACTIVE,
    /** 일시 정지. 결제일은 넘어가지만 청구 기록은 남기지 않는다 */
    PAUSED,
    CANCELED
}
