package org.routineslim.domain.subscription.scan;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/**
 * 영수증에서 꺼낸 값. 못 읽은 항목은 null.
 *
 * @param storeName 상호명 (예: NETFLIX.COM, Apple)
 * @param itemNames 품목 이름들 (앱스토어 영수증은 상호가 Apple 이고 품목에 서비스 이름이 온다)
 * @param amount    최종 결제 금액, 원
 * @param paidDate  결제일
 */
public record ParsedReceipt(
        String storeName,
        List<String> itemNames,
        Integer amount,
        LocalDate paidDate
) {

    /** 프리셋 매칭에 쓸 글자들. 상호명을 먼저 본다 */
    public List<String> matchTexts() {
        return storeName == null
                ? itemNames
                : Stream.concat(Stream.of(storeName), itemNames.stream()).toList();
    }

    public boolean isEmpty() {
        return storeName == null && itemNames.isEmpty() && amount == null && paidDate == null;
    }
}
