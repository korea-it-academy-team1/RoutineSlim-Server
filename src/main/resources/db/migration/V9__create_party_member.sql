-- 파티 참여 (앱: 커뮤니티 > 파티원 모집 글 > '참여 요청하기', 마이 > 커뮤니티 활동 > 참여 중인 파티)
-- 회원이 파티원 모집 글에 참여를 요청하면 REQUESTED, 작성자가 수락하면 ACCEPTED(글의 현재 인원 +1), 거절하면 REJECTED.
-- 요청을 취소하거나 파티에서 나가면 행을 지운다 (수락된 상태였으면 현재 인원 -1). 글당 회원 1건.
CREATE TABLE party_member (
    party_member_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '파티 참여 번호',
    post_id         BIGINT      NOT NULL COMMENT '파티원 모집 게시글 번호',
    member_id       BINARY(16)  NOT NULL COMMENT '참여를 요청한 회원',
    status          VARCHAR(10) NOT NULL COMMENT 'REQUESTED 요청 / ACCEPTED 수락 / REJECTED 거절',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '요청일시',
    updated_at      DATETIME    NULL     ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    CONSTRAINT pk_party_member PRIMARY KEY (party_member_id),
    CONSTRAINT uk_party_member_post_member UNIQUE (post_id, member_id),
    CONSTRAINT ck_party_member_status CHECK (status IN ('REQUESTED', 'ACCEPTED', 'REJECTED')),
    CONSTRAINT fk_party_member_post FOREIGN KEY (post_id) REFERENCES post (post_id),
    CONSTRAINT fk_party_member_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    INDEX ix_party_member_member (member_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '파티 참여';
