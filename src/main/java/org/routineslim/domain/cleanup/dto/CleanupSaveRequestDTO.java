package org.routineslim.domain.cleanup.dto;

import lombok.*;

import java.util.List;
import java.util.UUID;

// 정리 목록에 담기 요청 (여러 구독을 한 번에)
// 예) {"items":[{"subscriptionId":"019a...","action":"CANCEL"},
//              {"subscriptionId":"019b...","action":"DOWNGRADE","newMonthlyAmount":5500}]}
// - action 이 KEEP 이면 그 구독을 목록에서 뺀다 (화면의 '그대로 두기')
// - newMonthlyAmount: DOWNGRADE·PARTY 일 때 필수 (정리 후 월 금액). CANCEL 은 무시하고 0
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CleanupSaveRequestDTO {

    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {

        // 구독 아이디
        private UUID subscriptionId;

        // CANCEL, DOWNGRADE, PARTY, KEEP
        private String action;

        // 정리 후 월 금액 (DOWNGRADE·PARTY)
        private Integer newMonthlyAmount;
    }
}
