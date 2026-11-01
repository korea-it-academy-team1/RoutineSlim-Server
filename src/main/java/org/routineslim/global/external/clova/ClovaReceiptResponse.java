package org.routineslim.global.external.clova;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * CLOVA OCR 영수증 응답 중 필요한 부분만 옮긴 것. 나머지 필드는 무시한다.
 * 구조: images[].receipt.result.{storeInfo, paymentInfo, subResults, totalPrice}
 * https://api.ncloud-docs.com/docs/ai-application-service-ocr-ocrdocumentocr-receipt
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClovaReceiptResponse(List<Image> images) {

    /** inferResult: SUCCESS / FAILURE / ERROR */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String name, String inferResult, String message, Receipt receipt) {

        public boolean isSuccess() {
            return "SUCCESS".equals(inferResult) && receipt != null && receipt.result() != null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Receipt(Result result) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Result(
            StoreInfo storeInfo,
            PaymentInfo paymentInfo,
            List<SubResult> subResults,
            TotalPrice totalPrice
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StoreInfo(Field name, Field subName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentInfo(DateField date) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubResult(List<Item> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(Field name, ItemPrice price) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemPrice(Field price, Field unitPrice) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TotalPrice(Field price, Field creditCardPrice) {
    }

    /** 공통 필드: 원문(text)과 정규화한 값(formatted.value) */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Field(String text, Formatted formatted, Double confidenceScore) {

        /** 정규화한 값이 있으면 그것을, 없으면 원문을 돌려준다 */
        public String bestValue() {
            if (formatted != null && formatted.value() != null && !formatted.value().isBlank()) {
                return formatted.value();
            }
            return text;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Formatted(String value) {
    }

    /** 날짜 필드: formatted 에 value 대신 year, month, day 가 온다 */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateField(String text, DateFormatted formatted) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DateFormatted(String year, String month, String day) {
    }
}
