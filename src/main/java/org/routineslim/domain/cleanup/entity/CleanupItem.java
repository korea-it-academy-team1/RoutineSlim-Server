package org.routineslim.domain.cleanup.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "cleanup_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        // 구독당 1건
                        name = "uk_cleanup_item_sub",
                        columnNames = {"sub_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleanupItem {

    // 정리 항목 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cleanup_item_id")
    private Long cleanupItemId;

    // 구독 아이디
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "sub_id",
            columnDefinition = "binary(16)",
            nullable = false)
    private UUID subscriptionId;

    // 회원 식별자
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id",
            columnDefinition = "binary(16)",
            nullable = false)
    private UUID memberId;

    // 정리 방법
    @Enumerated(EnumType.STRING)
    @Column(name = "action",
            nullable = false,
            length = 10)
    private CleanupAction action;

    // 담을 때의 월 환산 금액
    @Column(name = "monthly_amount",
            nullable = false,
            columnDefinition = "int unsigned")
    private Integer monthlyAmount;

    // 정리 후 월 금액 (해지는 0)
    @Column(name = "new_monthly_amount",
            nullable = false,
            columnDefinition = "int unsigned")
    private Integer newMonthlyAmount;

    // 알림 날짜 (다음 결제일 3일 전)
    @Column(name = "remind_on")
    private LocalDate remindOn;

    // 담은 일시 (저장 직후 응답에도 쓰도록 서비스에서 넣는다)
    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
}
