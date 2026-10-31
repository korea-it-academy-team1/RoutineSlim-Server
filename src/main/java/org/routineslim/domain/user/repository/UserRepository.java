package org.routineslim.domain.user.repository;

import org.routineslim.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    // 아이디 중복 확인
    boolean existsByMemberName(String memberName);

    // 로그인 - 아이디로 회원 조회
    Optional<User> findByMemberName(String memberName);

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);
}
