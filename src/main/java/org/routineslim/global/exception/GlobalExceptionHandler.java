package org.routineslim.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리.
 *
 * 서비스에서 입력값이 잘못됐을 때 던지는 IllegalArgumentException 을 500 대신 400 으로 돌려준다.
 * 응답은 다른 오류와 같은 ProblemDetail(RFC 9457) 형식이고, 예외 메시지가 detail 에 들어간다.
 * 앱은 detail 을 그대로 화면에 보여 준다 (src/api/client.ts).
 *   예) {"type":"about:blank","title":"Bad Request","status":400,"detail":"이미 사용 중인 이메일입니다.","instance":"/users/updateMyInfo"}
 *
 * ResponseStatusException, 검증 실패(@Validated) 등은 spring.mvc.problemdetails 가 이미 처리하므로 여기서 다루지 않는다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException e) {
        log.debug("잘못된 요청: {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
