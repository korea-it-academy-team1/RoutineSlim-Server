-- 사용량 점검 (앱: 마이 > 사용량 점검)
-- 다른 앱의 사용 시간은 읽을 수 없어서, 회원이 구독마다 최근 사용 정도를 직접 체크한다. 구독당 최신 1건만 둔다
CREATE TABLE usage_check (
    usage_check_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '사용량 체크 번호',
    sub_id         BINARY(16)  NOT NULL COMMENT '구독 아이디',
    member_id      BINARY(16)  NOT NULL COMMENT '회원 식별자',
    usage_level    VARCHAR(10) NOT NULL COMMENT 'OFTEN 자주 / SOMETIMES 가끔 / RARELY 거의 안 씀 / NEVER 안 씀',
    checked_on     DATE        NOT NULL COMMENT '체크한 날',
    created_at     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_at     DATETIME    NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_usage_check PRIMARY KEY (usage_check_id),
    CONSTRAINT uk_usage_check_sub UNIQUE (sub_id),
    CONSTRAINT ck_usage_check_level CHECK (usage_level IN ('OFTEN', 'SOMETIMES', 'RARELY', 'NEVER')),
    CONSTRAINT fk_usage_check_subscription FOREIGN KEY (sub_id) REFERENCES subscription (sub_id),
    CONSTRAINT fk_usage_check_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    INDEX ix_usage_check_member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '사용량 점검';
