package org.routineslim.domain.subscription.scan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.routineslim.global.external.clova.ClovaReceiptResponse;
import org.routineslim.global.external.clova.ClovaReceiptResponse.DateField;
import org.routineslim.global.external.clova.ClovaReceiptResponse.DateFormatted;
import org.routineslim.global.external.clova.ClovaReceiptResponse.Field;
import org.routineslim.global.external.clova.ClovaReceiptResponse.Formatted;
import org.routineslim.global.external.clova.ClovaReceiptResponse.Item;
import org.routineslim.global.external.clova.ClovaReceiptResponse.ItemPrice;

class ReceiptParserTest {

    @Test
    void 상호_품목_금액_결제일을_꺼낸다() {
        ParsedReceipt parsed = ReceiptParser.parse(image(
                field("NETFLIX.COM"),
                List.of(new Item(field("프리미엄 멤버십"), new ItemPrice(field("17,000", "17000"), null))),
                new ClovaReceiptResponse.TotalPrice(field("17,000", "17000"), null),
                date("2026", "10", "12"))).orElseThrow();

        assertEquals("NETFLIX.COM", parsed.storeName());
        assertEquals(List.of("프리미엄 멤버십"), parsed.itemNames());
        assertEquals(17000, parsed.amount());
        assertEquals(LocalDate.of(2026, 10, 12), parsed.paidDate());
        assertEquals(List.of("NETFLIX.COM", "프리미엄 멤버십"), parsed.matchTexts());
    }

    @Test
    void 총액이_없으면_카드_승인금액_그다음_단일_품목_금액을_쓴다() {
        ParsedReceipt byCard = ReceiptParser.parse(image(null, List.of(),
                new ClovaReceiptResponse.TotalPrice(null, field("10,900원")), null)).orElseThrow();
        assertEquals(10900, byCard.amount());

        ParsedReceipt byItem = ReceiptParser.parse(image(field("Apple"),
                List.of(new Item(field("YouTube Premium"), new ItemPrice(field("14,900"), null))),
                null, null)).orElseThrow();
        assertEquals(14900, byItem.amount());
    }

    @Test
    void 소수점_금액과_두자리_연도를_처리한다() {
        ParsedReceipt parsed = ReceiptParser.parse(image(field("Spotify"), List.of(),
                new ClovaReceiptResponse.TotalPrice(field("10900.00"), null), date("26", "1", "5"))).orElseThrow();
        assertEquals(10900, parsed.amount());
        assertEquals(LocalDate.of(2026, 1, 5), parsed.paidDate());
    }

    @Test
    void 없는_날짜는_null() {
        ParsedReceipt parsed = ReceiptParser.parse(image(field("Spotify"), List.of(), null,
                date("2026", "2", "30"))).orElseThrow();
        assertNull(parsed.paidDate());
    }

    @Test
    void 인식_실패나_빈_결과는_비어있다() {
        assertTrue(ReceiptParser.parse(new ClovaReceiptResponse.Image("r", "ERROR", "Read page:0 error.", null))
                .isEmpty());
        assertTrue(ReceiptParser.parse(image(null, List.of(), null, null)).isEmpty());
    }

    private static ClovaReceiptResponse.Image image(Field storeName, List<Item> items,
                                                    ClovaReceiptResponse.TotalPrice totalPrice, DateField date) {
        ClovaReceiptResponse.Result result = new ClovaReceiptResponse.Result(
                new ClovaReceiptResponse.StoreInfo(storeName, null),
                new ClovaReceiptResponse.PaymentInfo(date),
                List.of(new ClovaReceiptResponse.SubResult(items)),
                totalPrice);
        return new ClovaReceiptResponse.Image("receipt", "SUCCESS", "SUCCESS", new ClovaReceiptResponse.Receipt(result));
    }

    private static Field field(String text) {
        return new Field(text, null, null);
    }

    private static Field field(String text, String formatted) {
        return new Field(text, new Formatted(formatted), 0.9);
    }

    private static DateField date(String year, String month, String day) {
        return new DateField(year + "-" + month + "-" + day, new DateFormatted(year, month, day));
    }
}
