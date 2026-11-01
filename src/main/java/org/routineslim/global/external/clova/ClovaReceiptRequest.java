package org.routineslim.global.external.clova;

import java.util.List;
import java.util.UUID;

/** CLOVA OCR 영수증 요청 본문 (JSON, 이미지는 Base64) */
public record ClovaReceiptRequest(
        String version,
        String requestId,
        long timestamp,
        List<Image> images
) {

    public static ClovaReceiptRequest of(String format, String base64Data) {
        return new ClovaReceiptRequest(
                "V2",
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                List.of(new Image(format, "receipt", base64Data))
        );
    }

    /** format: jpg, jpeg, png, pdf (pdf 는 1쪽만) */
    public record Image(String format, String name, String data) {
    }
}
