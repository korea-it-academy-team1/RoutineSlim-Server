package org.routineslim.domain.device.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name = "device",
        uniqueConstraints = {
                @UniqueConstraint(
                        // FCM 토큰 중복 방지
                        name = "uk_device_fcm_token",
                        columnNames = {"fcm_token"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    // 기기 고유번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    // 회원 식별자
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "member_id",
            columnDefinition = "binary(16)",
            nullable = false)
    private UUID memberId;

    // Firebase 푸시 알림 토큰
    @Column(name = "fcm_token",
            nullable = false,
            length = 512)
    private String fcmToken;

    // 기기별 알림 허용 여부
    @Column(name = "notification_enabled",
            nullable = false)
    private Boolean notificationEnabled;
}
