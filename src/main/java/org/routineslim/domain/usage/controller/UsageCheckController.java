package org.routineslim.domain.usage.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.routineslim.domain.usage.dto.UsageCheckDTO;
import org.routineslim.domain.usage.dto.UsageCheckRequestDTO;
import org.routineslim.domain.usage.service.UsageCheckService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/usage")
public class UsageCheckController {

    private final UsageCheckService usageCheckService;

    // 사용량 체크 목록 조회
    // 예) GET /usage
    @GetMapping
    public ResponseEntity<List<UsageCheckDTO>> getUsageChecks(
            @RequestHeader("X-Member-Id") UUID memberId
    ) {

        // 회원의 구독별 최근 체크 조회
        List<UsageCheckDTO> response = usageCheckService.getUsageChecks(memberId);

        // 정상 처리 시 HTTP 200 반환
        return ResponseEntity.ok(response);
    }

    // 구독의 사용량 체크 저장 (있으면 수정)
    // 예) PUT /usage/{subscriptionId}  {"usageLevel": "RARELY"}
    @PutMapping("/{subscriptionId}")
    public ResponseEntity<UsageCheckDTO> saveUsageCheck(
            @RequestHeader("X-Member-Id") UUID memberId,
            @PathVariable("subscriptionId") UUID subscriptionId,
            @RequestBody UsageCheckRequestDTO usageCheckRequestDTO
    ) {

        // 사용량 체크 저장
        UsageCheckDTO response = usageCheckService.saveUsageCheck(memberId, subscriptionId, usageCheckRequestDTO);

        // 정상 처리 시 HTTP 200 반환
        return ResponseEntity.ok(response);
    }
}
