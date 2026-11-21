package org.routineslim.domain.cleanup.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.cleanup.dto.CleanupItemDTO;
import org.routineslim.domain.cleanup.dto.CleanupSaveRequestDTO;
import org.routineslim.domain.cleanup.service.CleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// 정리 목록 (앱: 사용량 점검 → 정리 방법 선택 → 정리 전후 비교 → '정리 목록에 담기')
// 주소는 사용량 점검(/usage)과 같은 형식
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/cleanup")
public class CleanupController {

    private final CleanupService cleanupService;

    // 정리 목록 조회 (담은 순, 해지·삭제된 구독은 빠진다)
    // 예) GET /cleanup
    @GetMapping
    public ResponseEntity<List<CleanupItemDTO>> getCleanupItems(
            @RequestHeader("X-Member-Id") UUID memberId
    ) {
        return ResponseEntity.ok(cleanupService.getCleanupItems(memberId));
    }

    // 정리 목록에 담기 (구독마다 덮어쓰기, KEEP 은 빼기) → 저장 후 전체 목록
    // 예) POST /cleanup  {"items":[{"subscriptionId":"019a...","action":"CANCEL"}]}
    @PostMapping
    public ResponseEntity<List<CleanupItemDTO>> saveCleanupItems(
            @RequestHeader("X-Member-Id") UUID memberId,
            @RequestBody CleanupSaveRequestDTO request
    ) {
        return ResponseEntity.ok(cleanupService.saveCleanupItems(memberId, request));
    }

    // 정리 목록에서 빼기 → HTTP 204
    // 예) DELETE /cleanup/019a...
    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<Void> deleteCleanupItem(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("subscriptionId") UUID subscriptionId
    ) {
        cleanupService.deleteCleanupItem(memberId, subscriptionId);
        return ResponseEntity.noContent().build();
    }
}
