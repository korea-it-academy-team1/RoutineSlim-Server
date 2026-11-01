package org.routineslim.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.routineslim.domain.user.dto.UserResponseDTO;
import org.routineslim.domain.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/* [로그인 - 이대수] UserService 구현 */
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
