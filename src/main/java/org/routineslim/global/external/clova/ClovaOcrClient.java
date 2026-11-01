package org.routineslim.global.external.clova;

import java.util.Base64;

import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import lombok.extern.slf4j.Slf4j;

/**
 * CLOVA OCR 영수증 API 호출.
 * Secret Key 가 앱에 들어가지 않도록 앱은 서버를 거쳐서만 OCR을 쓴다.
 * 이미지는 호출에만 쓰고 저장하지 않는다.
 */
@Slf4j
@Component
public class ClovaOcrClient {

    private static final String SECRET_HEADER = "X-OCR-SECRET";

    private final ClovaOcrProperties properties;
    private final RestClient restClient;

    public ClovaOcrClient(ClovaOcrProperties properties) {
        this.properties = properties;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        this.restClient = RestClient.builder().requestFactory(requestFactory).build();
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    /**
     * 영수증 이미지 한 장을 분석한다.
     *
     * @param image  이미지 바이트
     * @param format jpg, jpeg, png, pdf
     * @return 이미지 한 장의 결과 (inferResult 가 SUCCESS 인지는 호출한 쪽에서 확인)
     */
    public ClovaReceiptResponse.Image analyzeReceipt(byte[] image, String format) {
        if (!isConfigured()) {
            throw new ClovaOcrException(ClovaOcrException.Reason.NOT_CONFIGURED,
                    "clova.ocr.url / clova.ocr.secret 설정이 없습니다.", null);
        }

        ClovaReceiptRequest request = ClovaReceiptRequest.of(format, Base64.getEncoder().encodeToString(image));
        ClovaReceiptResponse response;
        try {
            response = restClient.post()
                    .uri(properties.url())
                    .header(SECRET_HEADER, properties.secret())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ClovaReceiptResponse.class);
        } catch (RestClientResponseException e) {
            log.warn("CLOVA OCR 오류 응답: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new ClovaOcrException(ClovaOcrException.Reason.ERROR_RESPONSE,
                    "CLOVA OCR 오류 응답: " + e.getStatusCode(), e);
        } catch (ResourceAccessException e) {
            log.warn("CLOVA OCR 연결 실패 또는 시간 초과: {}", e.getMessage());
            throw new ClovaOcrException(ClovaOcrException.Reason.TIMEOUT, "CLOVA OCR 연결 실패 또는 시간 초과", e);
        }

        if (response == null || response.images() == null || response.images().isEmpty()) {
            throw new ClovaOcrException(ClovaOcrException.Reason.ERROR_RESPONSE, "CLOVA OCR 응답에 결과가 없습니다.", null);
        }
        return response.images().get(0);
    }
}
