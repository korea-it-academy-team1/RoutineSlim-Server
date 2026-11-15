package org.routineslim.domain.usage.service;

import org.routineslim.domain.usage.dto.UsageCheckDTO;
import org.routineslim.domain.usage.dto.UsageCheckRequestDTO;

import java.util.List;
import java.util.UUID;

public interface UsageCheckService {

    // 회원의 사용량 체크 목록 조회
    List<UsageCheckDTO> getUsageChecks(UUID memberId);

    // 구독의 사용량 체크 저장 (있으면 수정)
    UsageCheckDTO saveUsageCheck(UUID memberId, UUID subscriptionId, UsageCheckRequestDTO usageCheckRequestDTO);

}
