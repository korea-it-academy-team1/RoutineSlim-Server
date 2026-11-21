-- =============================================================================
-- 커뮤니티 후기(REVIEW)·자유(FREE) 게시판 테스트 데이터 — 로컬 DB 전용
-- =============================================================================
-- Flyway 마이그레이션이 아니다 (db/migration 밖에 둠). 운영 DB 에서 실행하지 않는다.
-- 전제: Flyway V6 적용 + TEST_DATA.sql 의 테스트 회원(tester01~10) 이 있어야 한다.
-- 실행: DBeaver 에서 routineslim 스키마를 고르고 전체 실행 (Alt+X)
--
-- - 여러 번 실행해도 된다: 이 파일이 넣는 글(아래 제목 목록)을 먼저 지우고 다시 넣는다
-- - TEST_DATA.sql 12번 글(파티 3·후기 3·자유 4)은 건드리지 않는다
-- - 시각은 실행한 때(NOW) 기준으로 만든다 → 'N분 전 / N시간 전 / N일 전' 이 고르게 보인다
-- - tester01(김민지) 글이 후기·자유에 하나씩 있어 수정·삭제 메뉴(mine=true)를 확인할 수 있다
--
-- 회원 UUID: 019a3c2e-0000-7000-8000-000000000{101~110}  (u = 1~10)
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 이 파일이 예전에 넣은 글·댓글 지우기
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_seed_post;
CREATE TEMPORARY TABLE tmp_seed_post (post_id BIGINT PRIMARY KEY)
SELECT p.post_id
FROM post p
JOIN member m ON m.member_id = p.member_id AND m.member_name LIKE 'tester__'
WHERE p.title IN (
    '티빙 3개월 써 보고 남기는 솔직 후기',
    '쿠팡 와우 해지 방어 혜택 받은 후기',
    'iCloud+ 200GB 로 올린 후기',
    'YouTube Premium 가족 요금제 1년 후기',
    '밀키트 구독 한 달 해 봤어요',
    'Microsoft 365 연간 결제 후기',
    '네이버플러스 멤버십 vs 쿠팡 와우 비교',
    '멜론에서 스포티파이로 옮긴 후기',
    '구독료 한 달에 얼마 쓰세요?',
    '결제일 몰아서 관리하는 방법',
    '해지 버튼 찾기 제일 어려운 서비스',
    '연간 결제가 무조건 이득일까요?',
    '체험판 자동 결제 막는 팁',
    '가족이랑 구독 나눠 쓰는 분 계세요?',
    '이번 달 구독 정리 인증합니다',
    'OTT 하나만 남긴다면?'
);

DELETE FROM comment WHERE post_id IN (SELECT post_id FROM tmp_seed_post);
DELETE FROM post    WHERE post_id IN (SELECT post_id FROM tmp_seed_post);
DROP TEMPORARY TABLE tmp_seed_post;

-- -----------------------------------------------------------------------------
-- 1. 게시글 — 후기 8개, 자유 8개
--    u = 작성자(tester01~10), views = 조회수, minutes_ago = 몇 분 전에 썼는지
--    후기·자유 글은 파티 정보(service_name ~ settlement_day)를 넣지 않는다
-- -----------------------------------------------------------------------------
INSERT INTO post (member_id, board, title, content, view_count, created_at)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u)),
       r.board, r.title, r.content, r.views,
       NOW() - INTERVAL r.minutes_ago MINUTE
FROM (
    -- 후기 (REVIEW)
              SELECT 1 AS u, 'REVIEW' AS board, '티빙 3개월 써 보고 남기는 솔직 후기' AS title,
                     '야구 시즌에는 확실히 본전 뽑아요. 드라마는 볼 게 금방 떨어져서 비시즌에는 일시정지하는 게 나을 것 같아요.' AS content,
                     57 AS views, 12 AS minutes_ago
    UNION ALL SELECT 3, 'REVIEW', '쿠팡 와우 해지 방어 혜택 받은 후기',
                     '해지 신청 화면까지 가니까 한 달 무료 쿠폰을 주더라고요. 정말 해지할 생각이었으면 그냥 끝까지 누르시면 됩니다.',
                     88, 95
    UNION ALL SELECT 6, 'REVIEW', 'iCloud+ 200GB 로 올린 후기',
                     '사진이 50GB 를 넘어서 결국 올렸어요. 가족 공유가 돼서 혼자 쓰는 것보다 훨씬 이득입니다.',
                     23, 240
    UNION ALL SELECT 7, 'REVIEW', 'YouTube Premium 가족 요금제 1년 후기',
                     '광고 없는 것만으로도 만족도가 높아요. 유튜브 뮤직이 같이 와서 음악 앱 하나를 정리했습니다.\n\n단점은 가족 구성원이 같은 주소여야 한다는 점이에요.',
                     134, 600
    UNION ALL SELECT 6, 'REVIEW', '밀키트 구독 한 달 해 봤어요',
                     '매주 오니까 외식이 확실히 줄었어요. 대신 바쁜 주에는 냉장고에 쌓여서 격주 배송으로 바꿨습니다.',
                     41, 1500
    UNION ALL SELECT 3, 'REVIEW', 'Microsoft 365 연간 결제 후기',
                     '월 결제보다 2만 원 정도 아꼈어요. 1TB 클라우드가 포함이라 다른 클라우드 구독을 하나 끊었습니다.',
                     19, 2880
    UNION ALL SELECT 9, 'REVIEW', '네이버플러스 멤버십 vs 쿠팡 와우 비교',
                     '둘 다 써 봤는데 배송은 쿠팡, 적립은 네이버가 낫네요. 저는 쿠팡 하나만 남겼어요.',
                     76, 4320
    UNION ALL SELECT 2, 'REVIEW', '멜론에서 스포티파이로 옮긴 후기',
                     '추천 플레이리스트가 정말 좋아요. 국내 최신곡 가사 지원은 멜론이 더 낫습니다.',
                     32, 7200

    -- 자유 (FREE)
    UNION ALL SELECT 1, 'FREE', '구독료 한 달에 얼마 쓰세요?',
                     '정리해 보니 한 달에 6만 원이 넘더라고요. 다들 어느 정도 쓰시는지 궁금해요.',
                     102, 25
    UNION ALL SELECT 4, 'FREE', '결제일 몰아서 관리하는 방법',
                     '카드 결제일을 하나로 맞추고 구독도 그 근처로 옮겼더니 관리가 훨씬 편해졌어요.',
                     45, 180
    UNION ALL SELECT 5, 'FREE', '해지 버튼 찾기 제일 어려운 서비스',
                     '앱에서는 해지가 안 되고 웹으로 가야 하는 곳이 은근 많네요. 여러분이 겪은 최악은 어디인가요?',
                     160, 420
    UNION ALL SELECT 8, 'FREE', '연간 결제가 무조건 이득일까요?',
                     '싸긴 한데 중간에 안 쓰게 되면 손해잖아요. 1년 넘게 쓸 확신이 있을 때만 연간으로 하시나요?',
                     38, 900
    UNION ALL SELECT 2, 'FREE', '체험판 자동 결제 막는 팁',
                     '체험 시작하자마자 해지 예약해 두면 체험 기간은 그대로 쓰고 결제는 안 돼요. 서비스마다 다르니 확인은 필수!',
                     211, 1800
    UNION ALL SELECT 10, 'FREE', '가족이랑 구독 나눠 쓰는 분 계세요?',
                     '부모님이랑 OTT 를 같이 쓰려는데 정산을 어떻게 하시는지 궁금해요.',
                     14, 3000
    UNION ALL SELECT 7, 'FREE', '이번 달 구독 정리 인증합니다',
                     '안 쓰던 웨이브랑 바이브 정리해서 월 19,400원 줄였어요. 사용량 점검 기능 덕분입니다.',
                     67, 5760
    UNION ALL SELECT 4, 'FREE', 'OTT 하나만 남긴다면?',
                     '넷플릭스, 티빙, 디즈니+ 중에 하나만 남겨야 한다면 뭘 고르시겠어요?',
                     95, 10080
) r;

-- -----------------------------------------------------------------------------
-- 2. 댓글 — u = 댓글 작성자, minutes_after = 글 작성 몇 분 뒤
--    글 작성 시각 + minutes_after 가 지금보다 늦으면 넣지 않는다 (미래 댓글 방지)
-- -----------------------------------------------------------------------------
INSERT INTO comment (post_id, member_id, content, created_at)
SELECT p.post_id,
       UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + c.u)),
       c.content,
       p.created_at + INTERVAL c.minutes_after MINUTE
FROM post p
JOIN member m ON m.member_id = p.member_id AND m.member_name LIKE 'tester__'
JOIN (
              SELECT '티빙 3개월 써 보고 남기는 솔직 후기' AS title, 4 AS u, '저도 비시즌엔 일시정지해요!' AS content, 5 AS minutes_after
    UNION ALL SELECT '쿠팡 와우 해지 방어 혜택 받은 후기', 5, '오 저도 해 봐야겠네요', 20
    UNION ALL SELECT '쿠팡 와우 해지 방어 혜택 받은 후기', 9, '저는 쿠폰 안 주던데 계정마다 다른가 봐요', 50
    UNION ALL SELECT 'YouTube Premium 가족 요금제 1년 후기', 1, '유튜브 뮤직 포함인 거 몰랐어요. 감사합니다', 30
    UNION ALL SELECT 'YouTube Premium 가족 요금제 1년 후기', 8, '주소 조건 때문에 저는 못 쓰고 있어요 ㅠㅠ', 120
    UNION ALL SELECT 'YouTube Premium 가족 요금제 1년 후기', 3, '광고 없는 건 진짜 신세계죠', 300
    UNION ALL SELECT '밀키트 구독 한 달 해 봤어요', 10, '격주 배송 좋은 방법이네요', 60
    UNION ALL SELECT '네이버플러스 멤버십 vs 쿠팡 와우 비교', 6, '저는 반대로 네이버만 남겼어요', 90
    UNION ALL SELECT '구독료 한 달에 얼마 쓰세요?', 2, '저는 4만 원 정도요', 3
    UNION ALL SELECT '구독료 한 달에 얼마 쓰세요?', 7, '정리하기 전엔 8만 원이었어요...', 10
    UNION ALL SELECT '구독료 한 달에 얼마 쓰세요?', 5, '앱에서 월 합계 보고 깜짝 놀랐어요', 18
    UNION ALL SELECT '결제일 몰아서 관리하는 방법', 1, '좋은 팁이네요. 따라 해 볼게요', 40
    UNION ALL SELECT '해지 버튼 찾기 제일 어려운 서비스', 3, '통신사 부가서비스가 제일 어려웠어요', 15
    UNION ALL SELECT '해지 버튼 찾기 제일 어려운 서비스', 6, '전화로만 해지되는 곳도 있더라고요', 45
    UNION ALL SELECT '해지 버튼 찾기 제일 어려운 서비스', 9, '공감합니다 ㅋㅋ', 200
    UNION ALL SELECT '연간 결제가 무조건 이득일까요?', 3, '저는 2년 이상 쓴 것만 연간으로 바꿔요', 25
    UNION ALL SELECT '체험판 자동 결제 막는 팁', 8, '이거 진짜 꿀팁이에요', 30
    UNION ALL SELECT '체험판 자동 결제 막는 팁', 4, '해지 예약하면 바로 끊기는 서비스도 있으니 조심하세요!', 240
    UNION ALL SELECT '가족이랑 구독 나눠 쓰는 분 계세요?', 1, '저는 매달 정산일에 계좌이체 받아요', 35
    UNION ALL SELECT '이번 달 구독 정리 인증합니다', 2, '대단해요 👏', 60
    UNION ALL SELECT 'OTT 하나만 남긴다면?', 1, '넷플릭스요. 볼 게 제일 많아요', 15
    UNION ALL SELECT 'OTT 하나만 남긴다면?', 5, '저는 티빙! 예능 때문에요', 70
    UNION ALL SELECT 'OTT 하나만 남긴다면?', 8, '디즈니+ 마블 팬이라...', 400
) c ON c.title = p.title
WHERE p.created_at + INTERVAL c.minutes_after MINUTE <= NOW();

-- -----------------------------------------------------------------------------
-- 확인
-- -----------------------------------------------------------------------------
SELECT p.board,
       COUNT(*)                                                       AS posts,
       SUM((SELECT COUNT(*) FROM comment c WHERE c.post_id = p.post_id)) AS comments
FROM post p
GROUP BY p.board
ORDER BY p.board;
