package org.routineslim.domain.usage.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "usage_check",
        uniqueConstraints = {
                @UniqueConstraint(
                        // 구독당 최신 체크 1건
                        name = "uk_usage_check_sub",
                        columnNames = {"sub_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageCheck {

    // 사용량 체크 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_check_id")
    private Long usageCheckId;

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

    // 최근 사용 정도
    @Enumerated(EnumType.STRING)
    @Column(name = "usage_level",
            nullable = false,
            length = 10)
    private UsageLevel usageLevel;

    // 체크한 날
    @Column(name = "checked_on",
            nullable = false)
    private LocalDate checkedOn;
}
