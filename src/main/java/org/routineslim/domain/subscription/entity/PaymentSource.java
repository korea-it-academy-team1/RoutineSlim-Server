package org.routineslim.domain.subscription.entity;

/** 청구 기록이 만들어진 경로 */
public enum PaymentSource {
    /** 결제일이 지나 배치가 만든 기록 */
    AUTO,
    /** 사용자가 직접 입력 */
    MANUAL,
    /** 결제 알림 파싱 (실험 기능) */
    PARSED
}
