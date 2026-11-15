package org.routineslim.domain.usage.repository;

import org.routineslim.domain.usage.entity.UsageCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsageCheckRepository extends JpaRepository<UsageCheck, Long> {

    // 구독의 체크 조회
    Optional<UsageCheck> findBySubscriptionId(UUID subscriptionId);

    // 회원의 모든 체크 조회
    List<UsageCheck> findByMemberId(UUID memberId);
}
