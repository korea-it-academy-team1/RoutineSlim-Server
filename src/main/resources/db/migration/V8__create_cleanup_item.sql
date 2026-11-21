-- 정리 목록 (앱: 사용량 점검 > 정리 방법 선택 > 정리 전후 비교 > '정리 목록에 담기')
-- 회원이 "이 구독은 해지(또는 요금제 변경·파티원 모집)하겠다"고 담아 둔 목록. 구독당 1건만 두고, 다시 담으면 덮어쓴다.
-- 실제 해지는 회원이 서비스에서 직접 하고, 앱에서 '해지 완료'를 누르면 구독 상태를 CANCELED 로 바꾼 뒤 이 행을 지운다.
-- remind_on: 다음 결제일 3일 전. 푸시 알림 발송(스케줄러·FCM)이 생기면 이 날짜에 "정리하기로 한 구독이 곧 결제돼요" 를 보낸다.
CREATE TABLE cleanup_item (
    cleanup_item_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '정리 항목 번호',
    sub_id             BINARY(16)   NOT NULL COMMENT '구독 아이디',
    member_id          BINARY(16)   NOT NULL COMMENT '회원 식별자',
    action             VARCHAR(10)  NOT NULL COMMENT 'CANCEL 해지 / DOWNGRADE 저렴한 요금제 / PARTY 파티원 모집',
    monthly_amount     INT UNSIGNED NOT NULL COMMENT '담을 때의 월 환산 금액 (원)',
    new_monthly_amount INT UNSIGNED NOT NULL COMMENT '정리 후 월 금액 (원). 해지는 0',
    remind_on          DATE         NULL     COMMENT '알림 날짜 (다음 결제일 3일 전)',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '담은 일시',
    updated_at         DATETIME     NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_cleanup_item PRIMARY KEY (cleanup_item_id),
    CONSTRAINT uk_cleanup_item_sub UNIQUE (sub_id),
    CONSTRAINT ck_cleanup_item_action CHECK (action IN ('CANCEL', 'DOWNGRADE', 'PARTY')),
    CONSTRAINT fk_cleanup_item_subscription FOREIGN KEY (sub_id) REFERENCES subscription (sub_id),
    CONSTRAINT fk_cleanup_item_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    INDEX ix_cleanup_item_member (member_id),
    INDEX ix_cleanup_item_remind (remind_on)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '정리 목록';
