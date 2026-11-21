-- 결제 알림 발송 이력
-- 같은 구독의 같은 결제 예정일에 동일 기기로 중복 발송하는 것을 방지한다.
CREATE TABLE notification_delivery (
    delivery_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '알림 발송 이력 번호',
    sub_id       BINARY(16)  NOT NULL COMMENT '구독 아이디',
    billing_date DATE        NOT NULL COMMENT '결제 예정일',
    device_id    BIGINT      NOT NULL COMMENT '기기 번호',
    status       VARCHAR(20) NOT NULL COMMENT 'PROCESSING 처리 중 / SENT 성공 / FAILED 실패',
    sent_at      DATETIME    NULL     COMMENT 'Firebase 전송 성공 시간',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '이력 생성 시간',

    CONSTRAINT pk_notification_delivery PRIMARY KEY (delivery_id),

    CONSTRAINT uk_notification_delivery
        UNIQUE (sub_id, billing_date, device_id),

    CONSTRAINT ck_notification_delivery_status
        CHECK (status IN ('PROCESSING', 'SENT', 'FAILED')),

    CONSTRAINT fk_notification_delivery_subscription
        FOREIGN KEY (sub_id) REFERENCES subscription (sub_id),

    CONSTRAINT fk_notification_delivery_device
        FOREIGN KEY (device_id) REFERENCES device (device_id),

    INDEX ix_notification_delivery_device (device_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '결제 알림 발송 이력';