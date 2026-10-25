-- RoutineSlim 초기 스키마 (MySQL 8.0)
-- 기준: 팀 ERD (테이블·컬럼 이름과 PK 타입은 ERD 그대로)
-- ERD에 없던 부분(기본값, FK, 인덱스, CHECK, 일부 컬럼, payment 테이블)은 ERD 초안 v0.2(HTML)에서 가져왔다.
-- 공통: InnoDB, utf8mb4 / utf8mb4_0900_ai_ci, 모든 FK에 인덱스

-- ---------------------------------------------------------------------------
-- 회원
-- ---------------------------------------------------------------------------
CREATE TABLE member (
    member_id           BINARY(16)       NOT NULL COMMENT '회원 식별자 (UUIDv7)',
    member_name         VARCHAR(50)      NOT NULL COMMENT '아이디',
    member_password     VARCHAR(255)     NOT NULL COMMENT '비밀번호 (bcrypt 해시)',
    name                VARCHAR(50)      NOT NULL COMMENT '이름',
    email               VARCHAR(255)     NULL     COMMENT '이메일',
    phone               VARCHAR(30)      NULL     COMMENT '전화번호',
    default_notify_days TINYINT UNSIGNED NOT NULL DEFAULT 3          COMMENT '기본 알림 시점 (D-n) [HTML]',
    notify_time         TIME             NOT NULL DEFAULT '09:00:00' COMMENT '알림 시각 [HTML]',
    created_at          DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '가입일',
    updated_at          DATETIME         NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    deleted_at          DATETIME         NULL     COMMENT '탈퇴 시각 [HTML]',
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_member_name UNIQUE (member_name),
    CONSTRAINT uk_member_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '회원';

CREATE TABLE social_account (
    social_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '소셜계정 번호',
    member_id        BINARY(16)   NOT NULL COMMENT '회원 식별자',
    provider         VARCHAR(20)  NOT NULL COMMENT '로그인 제공업체 (KAKAO, GOOGLE)',
    provider_user_id VARCHAR(255) NOT NULL COMMENT '사용자 고유 ID',
    CONSTRAINT pk_social_account PRIMARY KEY (social_id),
    CONSTRAINT uk_social_account_provider UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_social_account_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '소셜 계정';

CREATE TABLE refresh_token (
    token_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '토큰 번호',
    member_id   BINARY(16)   NOT NULL COMMENT '회원 식별자',
    token_hash  VARCHAR(255) NOT NULL COMMENT '토큰 해시 (원문 저장 금지)',
    device_name VARCHAR(100) NULL     COMMENT '기기 표시용 [HTML]',
    expires_at  DATETIME     NOT NULL COMMENT '만료일시',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    revoked_at  DATETIME     NULL     COMMENT '폐기일시 (로그아웃)',
    CONSTRAINT pk_refresh_token PRIMARY KEY (token_id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '로그인 세션';

CREATE TABLE device (
    device_id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '기기 번호',
    member_id            BINARY(16)   NOT NULL COMMENT '회원 식별자',
    platform             VARCHAR(10)  NOT NULL DEFAULT 'ANDROID' COMMENT 'ANDROID / IOS [HTML]',
    fcm_token            VARCHAR(512) NOT NULL COMMENT '푸시 토큰',
    notification_enabled BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '알림 허용 여부',
    app_version          VARCHAR(20)  NULL     COMMENT '[HTML]',
    last_synced_at       DATETIME     NULL     COMMENT '마지막 동기화 시각 [HTML]',
    created_at           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '[HTML]',
    CONSTRAINT pk_device PRIMARY KEY (device_id),
    CONSTRAINT uk_device_fcm_token UNIQUE (fcm_token),
    CONSTRAINT ck_device_platform CHECK (platform IN ('ANDROID', 'IOS')),
    CONSTRAINT fk_device_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '기기 (2단계)';

-- ---------------------------------------------------------------------------
-- 카테고리·프리셋
-- ---------------------------------------------------------------------------
CREATE TABLE subscription_category (
    sub_ctg_id    BIGINT      NOT NULL AUTO_INCREMENT COMMENT '구독 카테고리 번호',
    category_name VARCHAR(50) NOT NULL COMMENT '구독 카테고리 명',
    color         CHAR(7)     NULL     COMMENT '차트 색 #RRGGBB [HTML]',
    icon          VARCHAR(50) NULL     COMMENT '아이콘 키 [HTML]',
    sort_order    SMALLINT    NOT NULL DEFAULT 0 COMMENT '표시 순서 [HTML]',
    CONSTRAINT pk_subscription_category PRIMARY KEY (sub_ctg_id),
    CONSTRAINT uk_subscription_category_name UNIQUE (category_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '구독 카테고리 (공용)';

CREATE TABLE service_preset (
    preset_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '서비스 번호',
    sub_ctg_id    BIGINT       NOT NULL COMMENT '구독 카테고리 번호',
    service_name  VARCHAR(100) NOT NULL COMMENT '서비스 이름',
    logo_url      VARCHAR(500) NULL     COMMENT '서비스 로고 URL',
    default_price INT UNSIGNED NULL     COMMENT '대표 요금, 원 [HTML]',
    billing_cycle VARCHAR(10)  NOT NULL DEFAULT 'MONTHLY' COMMENT '대표 결제 주기 [HTML]',
    website_url   VARCHAR(500) NULL     COMMENT '서비스 주소 [HTML]',
    cancel_url    VARCHAR(500) NULL     COMMENT '해지 안내 링크 [HTML]',
    is_active     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '목록 노출 여부 [HTML]',
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '앱 캐시 갱신 기준 [HTML]',
    CONSTRAINT pk_service_preset PRIMARY KEY (preset_id),
    CONSTRAINT uk_service_preset_name UNIQUE (service_name),
    CONSTRAINT ck_service_preset_cycle CHECK (billing_cycle IN ('WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT fk_service_preset_category FOREIGN KEY (sub_ctg_id) REFERENCES subscription_category (sub_ctg_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '인기 서비스 프리셋';

CREATE TABLE categories (
    categories_id BINARY(16)  NOT NULL COMMENT '카테고리 번호 (UUIDv7)',
    member_id     BINARY(16)  NOT NULL COMMENT '회원 식별자',
    name          VARCHAR(50) NULL     COMMENT '카테고리명',
    color         CHAR(7)     NULL     COMMENT '차트 색 #RRGGBB',
    icon          VARCHAR(50) NULL     COMMENT '아이콘키',
    sort_order    SMALLINT    NULL     DEFAULT 0 COMMENT '표시 순서',
    CONSTRAINT pk_categories PRIMARY KEY (categories_id),
    CONSTRAINT uk_categories_member_name UNIQUE (member_id, name),
    CONSTRAINT fk_categories_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '사용자 카테고리';

-- ---------------------------------------------------------------------------
-- 결제 수단
-- ---------------------------------------------------------------------------
CREATE TABLE card (
    card_id     BIGINT       NOT NULL AUTO_INCREMENT COMMENT '카드 번호',
    member_id   BINARY(16)   NOT NULL COMMENT '회원 식별자',
    card_name   VARCHAR(250) NULL     COMMENT '카드 이름',
    payment_day DATE         NULL     COMMENT '결제일',
    CONSTRAINT pk_card PRIMARY KEY (card_id),
    CONSTRAINT fk_card_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '카드';

CREATE TABLE expense_method (
    expense_method_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '결제 수단 번호',
    method_name       VARCHAR(30) NOT NULL COMMENT '결제 수단 이름',
    CONSTRAINT pk_expense_method PRIMARY KEY (expense_method_id),
    CONSTRAINT uk_expense_method_name UNIQUE (method_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '결제 수단 종류';

-- ---------------------------------------------------------------------------
-- 구독
-- ---------------------------------------------------------------------------
CREATE TABLE subscription (
    sub_id             BINARY(16)        NOT NULL COMMENT '구독 아이디 (UUIDv7, 앱에서 생성)',
    member_id          BINARY(16)        NOT NULL COMMENT '회원 식별자',
    sub_ctg_id         BIGINT            NOT NULL COMMENT '카테고리 번호',
    preset_id          BIGINT            NULL     COMMENT '프리셋에서 만들었으면 service_preset 번호 [HTML]',
    card_id            BIGINT            NULL     COMMENT '결제 카드 [HTML payment_method_id]',
    name               VARCHAR(100)      NOT NULL COMMENT '서비스 이름',
    amount             INT UNSIGNED      NOT NULL COMMENT '1회 결제 금액, 원',
    currency           CHAR(3)           NOT NULL DEFAULT 'KRW' COMMENT '통화',
    billing_cycle      VARCHAR(10)       NOT NULL COMMENT 'WEEKLY / MONTHLY / YEARLY',
    cycle_interval     SMALLINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '주기 배수',
    start_date         DATE              NOT NULL COMMENT '결제 시작일',
    billing_day        TINYINT UNSIGNED  NOT NULL COMMENT '원래 결제일 (말일 보정용)',
    next_billing_date  DATE              NOT NULL COMMENT '다음 결제일',
    status             VARCHAR(10)       NOT NULL DEFAULT 'ACTIVE' COMMENT 'TRIAL / ACTIVE / PAUSED / CANCELED',
    trial_end_date     DATE              NULL     COMMENT '체험판 종료일',
    notify_enabled     TINYINT(1)        NOT NULL DEFAULT 1 COMMENT '구독 알림여부',
    notify_days_before TINYINT UNSIGNED  NULL     COMMENT 'NULL이면 member.default_notify_days',
    memo               TEXT              NULL     COMMENT '메모',
    canceled_at        DATETIME(3)       NULL     COMMENT '해지일',
    created_at         DATETIME(3)       NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일',
    updated_at         DATETIME(3)       NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '수정일',
    deleted_at         DATETIME(3)       NULL     COMMENT '약한 삭제',
    CONSTRAINT pk_subscription PRIMARY KEY (sub_id),
    CONSTRAINT ck_subscription_cycle CHECK (billing_cycle IN ('WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT ck_subscription_interval CHECK (cycle_interval >= 1),
    CONSTRAINT ck_subscription_billing_day CHECK (billing_day BETWEEN 1 AND 31),
    CONSTRAINT ck_subscription_status CHECK (status IN ('TRIAL', 'ACTIVE', 'PAUSED', 'CANCELED')),
    CONSTRAINT fk_subscription_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_subscription_category FOREIGN KEY (sub_ctg_id) REFERENCES subscription_category (sub_ctg_id),
    CONSTRAINT fk_subscription_preset FOREIGN KEY (preset_id) REFERENCES service_preset (preset_id),
    CONSTRAINT fk_subscription_card FOREIGN KEY (card_id) REFERENCES card (card_id),
    INDEX ix_subscription_member_status (member_id, status),
    INDEX ix_subscription_next_billing (next_billing_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '구독';

-- [HTML] 구독 청구 기록. 요금이 바뀌어도 지난달 통계가 남도록 결제일마다 1건씩 쌓는다.
CREATE TABLE payment (
    payment_id BIGINT       NOT NULL AUTO_INCREMENT COMMENT '청구 기록 번호',
    sub_id     BINARY(16)   NOT NULL COMMENT '구독 아이디',
    member_id  BINARY(16)   NOT NULL COMMENT '회원 식별자 (통계 조회용 중복 저장)',
    billed_on  DATE         NOT NULL COMMENT '청구일',
    amount     INT UNSIGNED NOT NULL COMMENT '그때의 금액, 원',
    currency   CHAR(3)      NOT NULL DEFAULT 'KRW',
    source     VARCHAR(10)  NOT NULL DEFAULT 'AUTO' COMMENT 'AUTO / MANUAL / PARSED',
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    CONSTRAINT pk_payment PRIMARY KEY (payment_id),
    CONSTRAINT uk_payment_sub_billed UNIQUE (sub_id, billed_on),
    CONSTRAINT ck_payment_source CHECK (source IN ('AUTO', 'MANUAL', 'PARSED')),
    CONSTRAINT fk_payment_subscription FOREIGN KEY (sub_id) REFERENCES subscription (sub_id),
    CONSTRAINT fk_payment_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    INDEX ix_payment_member_billed (member_id, billed_on)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '구독 청구 기록';

CREATE TABLE recommendation (
    rec_id        BINARY(16)   NOT NULL COMMENT '제안 아이디 (UUIDv7)',
    sub_id        BINARY(16)   NOT NULL COMMENT '구독 아이디',
    member_id     BINARY(16)   NOT NULL COMMENT '회원 식별자',
    title         VARCHAR(200) NULL     COMMENT '제안 이름',
    rule_code     VARCHAR(50)  NULL     COMMENT '룰 코드 (DUPLICATE_CATEGORY, YEARLY_CHEAPER 등)',
    saving_amount INT UNSIGNED NULL     COMMENT '예상 월 절약액, 원',
    status        VARCHAR(10)  NULL     DEFAULT 'NEW' COMMENT 'NEW / DISMISSED / ACTED',
    created_at    DATETIME(3)  NULL     DEFAULT CURRENT_TIMESTAMP(3) COMMENT '생성일',
    CONSTRAINT pk_recommendation PRIMARY KEY (rec_id),
    CONSTRAINT fk_recommendation_subscription FOREIGN KEY (sub_id) REFERENCES subscription (sub_id),
    CONSTRAINT fk_recommendation_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '절약 추천 (2단계)';

-- ---------------------------------------------------------------------------
-- 지출·일정
-- ---------------------------------------------------------------------------
CREATE TABLE expense (
    expense_id        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '지출번호',
    member_id         BINARY(16)   NOT NULL COMMENT '회원 식별자',
    expense_method_id BIGINT       NOT NULL COMMENT '결제 수단 번호',
    card_id           BIGINT       NULL     COMMENT '카드번호',
    categories_id     BINARY(16)   NOT NULL COMMENT '카테고리 번호',
    amount            INT          NOT NULL COMMENT '지출 금액',
    description       VARCHAR(250) NULL     COMMENT '지출 내용',
    expense_date      DATE         NOT NULL COMMENT '지출 날짜',
    CONSTRAINT pk_expense PRIMARY KEY (expense_id),
    CONSTRAINT fk_expense_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_expense_method FOREIGN KEY (expense_method_id) REFERENCES expense_method (expense_method_id),
    CONSTRAINT fk_expense_card FOREIGN KEY (card_id) REFERENCES card (card_id),
    CONSTRAINT fk_expense_categories FOREIGN KEY (categories_id) REFERENCES categories (categories_id),
    INDEX ix_expense_member_date (member_id, expense_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '지출';

CREATE TABLE schedule (
    schedule_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '일정 번호',
    member_id      BINARY(16)   NOT NULL COMMENT '회원 식별자',
    title          VARCHAR(100) NOT NULL COMMENT '일정 제목',
    amount         INT          NOT NULL COMMENT '결제 예정 금액',
    category       VARCHAR(30)  NOT NULL COMMENT '구독, 고정비, 카드 결제, 기타',
    payment_date   DATETIME     NOT NULL COMMENT '결제 예정일',
    description    TEXT         NULL     COMMENT '메모',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일',
    updated_at     DATETIME     NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    repeat_type    VARCHAR(20)  NOT NULL DEFAULT 'NONE'    COMMENT 'NONE, MONTHLY, YEARLY',
    payment_status VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED, CANCELED',
    end_date       DATE         NULL     COMMENT '반복 종료일',
    CONSTRAINT pk_schedule PRIMARY KEY (schedule_id),
    CONSTRAINT ck_schedule_repeat CHECK (repeat_type IN ('NONE', 'MONTHLY', 'YEARLY')),
    CONSTRAINT ck_schedule_status CHECK (payment_status IN ('PENDING', 'COMPLETED', 'CANCELED')),
    CONSTRAINT fk_schedule_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    INDEX ix_schedule_member_date (member_id, payment_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '결제 일정';

-- ---------------------------------------------------------------------------
-- 커뮤니티
-- ---------------------------------------------------------------------------
CREATE TABLE post (
    post_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '게시글 번호',
    member_id  BINARY(16)   NOT NULL COMMENT '회원 식별자',
    title      VARCHAR(200) NOT NULL COMMENT '게시글 제목',
    content    TEXT         NOT NULL COMMENT '게시글 내용',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일',
    updated_at DATETIME     NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    CONSTRAINT pk_post PRIMARY KEY (post_id),
    CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '게시글';

CREATE TABLE comment (
    comment_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '댓글 번호',
    post_id    BIGINT   NOT NULL COMMENT '게시글 번호',
    content    TEXT     NOT NULL COMMENT '댓글 내용',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성일',
    updated_at DATETIME NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일',
    CONSTRAINT pk_comment PRIMARY KEY (comment_id),
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '댓글';
