package org.routineslim.domain.cleanup.entity;

// 정리 방법 ('그대로 두기'는 담지 않는다)
public enum CleanupAction {

    // 해지
    CANCEL,

    // 저렴한 요금제로 변경
    DOWNGRADE,

    // 파티원 모집 (나눠 내기)
    PARTY
}
