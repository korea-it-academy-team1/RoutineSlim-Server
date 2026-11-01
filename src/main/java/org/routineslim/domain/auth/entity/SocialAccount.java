package org.routineslim.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.routineslim.domain.user.entity.User;

@Entity
@Table(
        name = "social_account",
        uniqueConstraints = {
                @UniqueConstraint(
                        // 제약조건 이름
                        name = "uk_social_account_provider",
                        columnNames = {"provider", "provider_user_id"}),
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    // 소셜 계정 번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_id")
    private Long socialId;

    // 회원 식별자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    // 소셜 로그인 제공업체
    @Column(name = "provider", nullable = false, length = 20)
    private SocialProvider provider;

    // 소셜 제공업체에서 발급한 사용자 ID
    @Column(name = "provider_user_id", nullable = false, length = 255)
    private String providerUserId;
}
