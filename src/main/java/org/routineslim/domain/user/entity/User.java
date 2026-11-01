package org.routineslim.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(
                        // 제약조건 이름
                        name = "uk_member_member_name",
                        columnNames = {"member_name"}),
                @UniqueConstraint(
                        name = "uk_member_email",
                        columnNames = {"email"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // 회원 식별자
    @Id
    @Column(name = "member_id", columnDefinition = "BINARY(16)")
    private UUID memberId;

    // 아이디
    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    // 비밀번호
    @Column(name = "member_password", nullable = false, length = 255)
    private String memberPassword;

    // 이름
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    // 이메일
    @Column(name = "email", length = 255)
    private String email;

    // 전화번호
    @Column(name = "phone", length = 30)
    private String phone;

    // 기본 알림 일수
    @Builder.Default
    @Column(name = "default_notify_days", nullable = false)
    private Integer defaultNotifyDays = 3;

    // 알림 시간
    @Builder.Default
    @Column(name = "notify_time", nullable = false)
    private LocalTime notifyTime = LocalTime.of(9, 0);

    // 가입일
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false,
            columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    // 수정일
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // 탈퇴일
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
