-- 커뮤니티 (앱: 커뮤니티 탭 / 웹: 커뮤니티)
-- V1 의 post 에는 제목·내용만 있어서 게시판 구분, 파티원 모집 정보, 조회수를 추가한다.
-- 파티원 모집(PARTY) 글만 service_name ~ settlement_day 값이 있고, 후기·자유 글은 NULL
ALTER TABLE post
    ADD COLUMN board            VARCHAR(10)  NOT NULL DEFAULT 'FREE' COMMENT 'PARTY 파티원 모집 / REVIEW 후기 / FREE 자유' AFTER member_id,
    ADD COLUMN service_name     VARCHAR(100) NULL     COMMENT '모집하는 구독 서비스 이름' AFTER content,
    ADD COLUMN recruit_status   VARCHAR(10)  NULL     COMMENT 'RECRUITING 모집 중 / CLOSED 모집 완료' AFTER service_name,
    ADD COLUMN current_members  INT UNSIGNED NULL     COMMENT '현재 인원 (작성자 포함)' AFTER recruit_status,
    ADD COLUMN max_members      INT UNSIGNED NULL     COMMENT '모집 인원 (작성자 포함)' AFTER current_members,
    ADD COLUMN total_price      INT UNSIGNED NULL     COMMENT '서비스 전체 월 요금 (원)' AFTER max_members,
    ADD COLUMN price_per_member INT UNSIGNED NULL     COMMENT '1인당 월 분담금 (원)' AFTER total_price,
    ADD COLUMN settlement_day   INT UNSIGNED NULL     COMMENT '매월 정산일 (1~31)' AFTER price_per_member,
    ADD COLUMN view_count       INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '조회수' AFTER settlement_day,
    ADD CONSTRAINT ck_post_board CHECK (board IN ('PARTY', 'REVIEW', 'FREE')),
    ADD CONSTRAINT ck_post_recruit_status CHECK (recruit_status IS NULL OR recruit_status IN ('RECRUITING', 'CLOSED')),
    ADD INDEX ix_post_board_created (board, created_at);

-- 댓글 작성자. 이미 있는 댓글은 작성자를 알 수 없어 NULL 로 둔다 (앱·웹에는 '알 수 없음')
ALTER TABLE comment
    ADD COLUMN member_id BINARY(16) NULL COMMENT '회원 식별자 (작성자)' AFTER post_id,
    ADD CONSTRAINT fk_comment_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    ADD INDEX ix_comment_post_created (post_id, created_at);
