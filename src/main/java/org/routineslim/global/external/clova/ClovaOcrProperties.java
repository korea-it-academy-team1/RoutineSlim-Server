package org.routineslim.global.external.clova;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * CLOVA OCR 접속 정보 (application.yml 의 clova.ocr.*).
 * url, secret 은 코드나 Git에 두지 않고 application-local.yml 또는 환경 변수로 넣는다.
 *
 * @param url            API Gateway Invoke URL (영수증 도메인, .../document/receipt)
 * @param secret         X-OCR-SECRET 헤더 값
 * @param connectTimeout 연결 제한 시간
 * @param readTimeout    응답 제한 시간 (OCR 처리 시간 포함)
 */
@ConfigurationProperties(prefix = "clova.ocr")
public record ClovaOcrProperties(
        String url,
        String secret,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("15s") Duration readTimeout
) {

    public boolean isConfigured() {
        return url != null && !url.isBlank() && secret != null && !secret.isBlank();
    }
}
