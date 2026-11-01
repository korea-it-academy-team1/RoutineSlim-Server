package org.routineslim.domain.subscription.scan;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.routineslim.global.external.clova.ClovaReceiptResponse;

/**
 * CLOVA 영수증 결과에서 구독 등록에 필요한 값(상호, 품목, 금액, 결제일)을 꺼낸다.
 * 스프링 의존성이 없는 순수 로직이라 단위 테스트로 검증한다.
 */
public final class ReceiptParser {

    private ReceiptParser() {
    }

    /** 인식에 실패했거나 쓸 만한 값이 하나도 없으면 빈 Optional */
    public static Optional<ParsedReceipt> parse(ClovaReceiptResponse.Image image) {
        if (image == null || !image.isSuccess()) {
            return Optional.empty();
        }
        ClovaReceiptResponse.Result result = image.receipt().result();

        String storeName = Optional.ofNullable(result.storeInfo())
                .map(ClovaReceiptResponse.StoreInfo::name)
                .map(ClovaReceiptResponse.Field::bestValue)
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .orElse(null);

        List<ClovaReceiptResponse.Item> items = items(result);
        List<String> itemNames = items.stream()
                .map(ClovaReceiptResponse.Item::name)
                .filter(Objects::nonNull)
                .map(ClovaReceiptResponse.Field::bestValue)
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(s -> !s.isEmpty())
                .toList();

        ParsedReceipt parsed = new ParsedReceipt(storeName, itemNames, amount(result, items), paidDate(result));
        return parsed.isEmpty() ? Optional.empty() : Optional.of(parsed);
    }

    /** 총 결제 금액 → 카드 승인 금액 → (품목이 하나면) 품목 금액 순으로 찾는다 */
    static Integer amount(ClovaReceiptResponse.Result result, List<ClovaReceiptResponse.Item> items) {
        ClovaReceiptResponse.TotalPrice total = result.totalPrice();
        if (total != null) {
            Integer price = parseAmount(total.price());
            if (price != null) {
                return price;
            }
            Integer creditCardPrice = parseAmount(total.creditCardPrice());
            if (creditCardPrice != null) {
                return creditCardPrice;
            }
        }
        if (items.size() == 1 && items.get(0).price() != null) {
            return parseAmount(items.get(0).price().price());
        }
        return null;
    }

    static LocalDate paidDate(ClovaReceiptResponse.Result result) {
        if (result.paymentInfo() == null || result.paymentInfo().date() == null
                || result.paymentInfo().date().formatted() == null) {
            return null;
        }
        ClovaReceiptResponse.DateFormatted f = result.paymentInfo().date().formatted();
        Integer year = parseInt(f.year());
        Integer month = parseInt(f.month());
        Integer day = parseInt(f.day());
        if (year == null || month == null || day == null) {
            return null;
        }
        if (year < 100) {
            year += 2000; // '26' 처럼 두 자리로 찍힌 연도
        }
        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            return null;
        }
    }

    /** "17,000", "17000원", "17000.00" 같은 값을 원 단위 정수로 */
    static Integer parseAmount(ClovaReceiptResponse.Field field) {
        if (field == null || field.bestValue() == null) {
            return null;
        }
        String value = field.bestValue();
        int dot = value.indexOf('.');
        if (dot >= 0) {
            value = value.substring(0, dot);
        }
        return parseInt(value);
    }

    private static Integer parseInt(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty() || digits.length() > 9) {
            return null;
        }
        return Integer.parseInt(digits);
    }

    private static List<ClovaReceiptResponse.Item> items(ClovaReceiptResponse.Result result) {
        List<ClovaReceiptResponse.Item> items = new ArrayList<>();
        if (result.subResults() != null) {
            for (ClovaReceiptResponse.SubResult subResult : result.subResults()) {
                if (subResult != null && subResult.items() != null) {
                    subResult.items().stream().filter(Objects::nonNull).forEach(items::add);
                }
            }
        }
        return items;
    }
}
