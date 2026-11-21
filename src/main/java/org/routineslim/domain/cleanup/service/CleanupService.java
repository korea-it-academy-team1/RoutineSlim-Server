package org.routineslim.domain.cleanup.service;

import org.routineslim.domain.cleanup.dto.CleanupItemDTO;
import org.routineslim.domain.cleanup.dto.CleanupSaveRequestDTO;

import java.util.List;
import java.util.UUID;

public interface CleanupService {

    // 정리 목록 조회 (해지·삭제된 구독은 빠진다)
    List<CleanupItemDTO> getCleanupItems(UUID memberId);

    // 정리 목록에 담기 (구독마다 덮어쓰기, KEEP 은 빼기). 저장 후 전체 목록을 돌려준다
    List<CleanupItemDTO> saveCleanupItems(UUID memberId, CleanupSaveRequestDTO request);

    // 정리 목록에서 빼기
    void deleteCleanupItem(UUID memberId, UUID subscriptionId);
}
