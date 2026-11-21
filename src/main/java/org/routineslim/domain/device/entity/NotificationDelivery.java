package org.routineslim.domain.device.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.routineslim.domain.device.entity.enums.NotificationDeliveryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notification_delivery",
        uniqueConstraints = {
                @UniqueConstraint(
                        // 동일 구독, 동일 결제 예정일, 동일 기기의 중복 알림 방지
                        name = "uk_notification_delivery",
                        columnNames = {"sub_id", "billing_date", "device_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDelivery {

    // 알림 발송 이력 고유번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id")
    private Long deliveryId;

    // 구독 고유번호
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(
            name = "sub_id",
            columnDefinition = "binary(16)",
            nullable = false
    )
    private UUID subscriptionId;

    // 결제 예정일
    @Column(name = "billing_date", nullable = false)
    private LocalDate billingDate;

    // 알림을 받을 기기 고유번호
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    // 알림 발송 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationDeliveryStatus status;

    // 발송 성공 시간
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // 이력 생성 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
