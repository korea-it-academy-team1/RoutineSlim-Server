package org.routineslim.domain.cleanup.repository;

import org.routineslim.domain.cleanup.entity.CleanupItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CleanupItemRepository extends JpaRepository<CleanupItem, Long> {

    // 회원의 정리 목록 (담은 순)
    List<CleanupItem> findByMemberIdOrderByCreatedAtAscCleanupItemIdAsc(UUID memberId);

    // 구독의 정리 항목
    Optional<CleanupItem> findBySubscriptionId(UUID subscriptionId);
}
