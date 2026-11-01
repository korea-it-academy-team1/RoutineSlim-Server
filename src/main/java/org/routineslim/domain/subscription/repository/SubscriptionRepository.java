package org.routineslim.domain.subscription.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.routineslim.domain.subscription.entity.Subscription;
import org.routineslim.domain.subscription.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByIdAndMemberIdAndDeletedAtIsNull(UUID id, UUID memberId);

    List<Subscription> findByMemberIdAndDeletedAtIsNullOrderByNextBillingDateAscNameAsc(UUID memberId);

    List<Subscription> findByMemberIdAndStatusAndDeletedAtIsNullOrderByNextBillingDateAscNameAsc(
            UUID memberId, SubscriptionStatus status);

    /** 결제일이 지난(오늘 포함) 구독. 청구 기록 배치용 */
    List<Subscription> findByStatusInAndNextBillingDateLessThanEqualAndDeletedAtIsNull(
            Collection<SubscriptionStatus> statuses, LocalDate date);

    // ---------------------------------------------------------------------
    // 다른 도메인 테이블 존재 확인.
    // member·category·preset·card 엔티티가 아직 없어서 네이티브 쿼리로 확인한다.
    // 각 도메인 리포지토리가 생기면 그쪽 existsBy... 로 바꾼다.
    // ---------------------------------------------------------------------

    @Query(value = "SELECT COUNT(*) FROM member WHERE member_id = :memberId AND deleted_at IS NULL",
            nativeQuery = true)
    long countActiveMember(@Param("memberId") byte[] memberId);

    @Query(value = "SELECT COUNT(*) FROM subscription_category WHERE sub_ctg_id = :categoryId",
            nativeQuery = true)
    long countCategory(@Param("categoryId") Long categoryId);

    @Query(value = "SELECT COUNT(*) FROM service_preset WHERE preset_id = :presetId", nativeQuery = true)
    long countPreset(@Param("presetId") Long presetId);

    @Query(value = "SELECT COUNT(*) FROM card WHERE card_id = :cardId AND member_id = :memberId",
            nativeQuery = true)
    long countCardOfMember(@Param("cardId") Long cardId, @Param("memberId") byte[] memberId);

    @Query(value = "SELECT preset_id AS presetId, sub_ctg_id AS categoryId, service_name AS serviceName "
            + "FROM service_preset WHERE is_active = 1", nativeQuery = true)
    List<PresetCandidate> findActivePresetCandidates();
}
