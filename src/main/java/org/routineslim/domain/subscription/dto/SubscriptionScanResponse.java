package org.routineslim.domain.subscription.dto;

import java.time.LocalDate;
import java.util.List;

import org.routineslim.domain.subscription.entity.BillingCycle;

/**
 * 영수증 스캔 결과 = 구독 등록 화면에 미리 채울 초안. 서버에 저장하지 않음.
 * 사용자가 확인·수정한 뒤 POST /api/v1/subscriptions 로 등록.
 * 못 읽은 값은 null 이고, 앱은 그 칸을 비워 두고 사용자에게 입력.
 *
 * @param fileName        올린 파일 이름 (화면 표시용)
 * @param name            서비스 이름 (프리셋과 맞으면 프리셋 이름, 아니면 영수증 상호명)
 * @param presetId        맞은 프리셋 (없으면 null)
 * @param categoryId      프리셋의 카테고리 (없으면 null → 사용자가 선택)
 * @param amount          결제 금액, 원
 * @param billingCycle    영수증에는 주기가 없어서 MONTHLY 로 제안한다
 * @param startDate       영수증 결제일
 * @param nextBillingDate 결제일과 주기로 계산한 다음 결제일
 * @param presetMatched   프리셋과 맞았는지
 * @param recognized      OCR이 읽은 원래 글자 (사용자가 확인할 수 있게)
 */
public record SubscriptionScanResponse(
        String fileName,
        String name,
        Long presetId,
        Long categoryId,
        Integer amount,
        BillingCycle billingCycle,
        LocalDate startDate,
        LocalDate nextBillingDate,
        boolean presetMatched,
        Recognized recognized
) {

    public record Recognized(String storeName, List<String> itemNames) {
    }
}
