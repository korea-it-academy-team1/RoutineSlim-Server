package org.routineslim.domain.auth.repository;

import org.routineslim.domain.auth.entity.SocialAccount;
import org.routineslim.domain.auth.entity.enums.SocialProvider;
import org.routineslim.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountRepository  extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(
            SocialProvider provider,
            String providerUserId
    );

    // 해당 회원에게 연결된 소셜 계정이 있는지 확인
    boolean existsByMember(User member);
}
