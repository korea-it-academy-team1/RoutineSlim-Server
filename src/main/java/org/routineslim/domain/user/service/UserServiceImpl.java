package org.routineslim.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.user.dto.UserDTO;
import org.routineslim.domain.user.dto.UserResponseDTO;
import org.routineslim.domain.user.entity.User;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;

    // 조회
    @Override
    public UserDTO getMyInfo(UUID memberId) {

        // 탈퇴하지 않은 회원 조회
        User user = userRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        // 조회한 회원정보를 DTO로 변환하여 반환
        return UserDTO.builder()
                .memberId(user.getMemberId())
                .memberName(user.getMemberName())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .defaultNotificationDays(user.getDefaultNotifyDays())
                .notificationTime(user.getNotifyTime())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // 수정
    @Override
    public void updateMyInfo(UUID memberId, UserDTO userDTO) {

        // 탈퇴하지 않은 회원 조회
        User user = userRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        // 이름 필수 입력 검사
        if (userDTO.getName() == null || userDTO.getName().isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        // 이메일 중복 검사
        if (userDTO.getEmail() != null &&
                !userDTO.getEmail().isBlank() &&
                !userDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {

            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 회원정보 수정
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        // 회원정보 저장
        userRepository.save(user);
    }

    // 탈퇴
    @Override
    public void withdrawUser(UUID memberId) {

        // 회원번호로 회원 조회
        User user = userRepository.findByMemberIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException("회원을 찾을 수 없습니다.")
                );

        // 탈퇴 날짜 저장
        user.setDeletedAt(LocalDateTime.now());
    }

    // 회원정보 조회
    @Override
    public UserResponseDTO getUser(UUID memberId) {
        return userRepository.findById(memberId)
                // 탈퇴(deleted_at 있음)한 회원은 조회되지 않게 한다
                .filter(user -> user.getDeletedAt() == null)
                .map(UserResponseDTO::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다."));
    }
}
