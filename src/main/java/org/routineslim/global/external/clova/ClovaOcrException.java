package org.routineslim.global.external.clova;

/** CLOVA OCR 호출 실패 */
public class ClovaOcrException extends RuntimeException {

    public enum Reason {
        /** url, secret 설정이 없음 */
        NOT_CONFIGURED,
        /** 응답이 늦거나 연결 실패 */
        TIMEOUT,
        /** CLOVA 가 오류 응답을 보냄 (키 오류, 요청 형식 오류 등) */
        ERROR_RESPONSE
    }

    private final Reason reason;

    public ClovaOcrException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
