package org.routineslim.domain.community.entity;

// 파티 참여 상태
public enum PartyMemberStatus {

    // 참여 요청 (작성자 확인 전)
    REQUESTED,

    // 수락 (파티원)
    ACCEPTED,

    // 거절
    REJECTED
}
