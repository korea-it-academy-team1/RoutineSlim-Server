-- =============================================================================
-- 화면 확인용 테스트 데이터 (회원 10명 기준) — 로컬 DB 전용
-- =============================================================================
-- Flyway 마이그레이션이 아니다 (db/migration 밖에 둠). 운영 DB 에서 실행하지 않는다.
-- 실행: MySQL Workbench / DBeaver 에서 routineslim 스키마를 고르고 전체 실행
--       또는  mysql -u routineslim -p routineslim < TEST_DATA.sql
--
-- - 여러 번 실행해도 된다: 맨 앞에서 테스트 회원(tester01~10)의 데이터를 먼저 지우고 다시 넣는다
-- - 날짜는 실행한 날(CURDATE) 기준으로 만든다 → 언제 실행해도 홈의 '다가오는 결제'가 채워진다
-- - 공용 데이터(subscription_category, service_preset, expense_method)는 V2 마이그레이션 값을 그대로 쓴다
--
-- 로그인: 아이디 tester01 ~ tester10 / 비밀번호 qwert123!@ (admin 과 같은 BCrypt 해시)
-- 회원 UUID: 019a3c2e-0000-7000-8000-000000000101 ~ ...110  (admin 은 ...001)
-- 구독 UUID: 019a3c2e-0000-7000-8001-0000000{101~110}{01~06}
--
-- 회원별 특징 (화면 확인 포인트)
--   tester01 김민지  구독 6개, 오늘~7일 안 결제 4개 (홈 '다가오는 결제' 가득)
--   tester02 이서준  오늘 결제(D-Day) + 체험판(TRIAL)
--   tester03 박지우  연간 결제(YEARLY) 포함
--   tester04 최하은  일시정지(PAUSED) 포함 → 홈·캘린더에서 빠져야 함
--   tester05 정도윤  해지(CANCELED) 포함 → 홈·캘린더에서 빠져야 함
--   tester06 강예린  매주 결제(WEEKLY) 포함
--   tester07 조시우  카테고리 3가지 고르게
--   tester08 윤하린  체험판 + 30일 뒤 결제 (7일 안 결제 없음 → '7일 안에 결제할 구독이 없어요')
--   tester09 장도현  구독 1개
--   tester10 임수아  구독 없음 → 빈 화면 확인
-- =============================================================================

SET NAMES utf8mb4;

-- -----------------------------------------------------------------------------
-- 0. 이전 테스트 데이터 지우기 (FK 순서: 자식 → 부모)
-- -----------------------------------------------------------------------------
DROP TEMPORARY TABLE IF EXISTS tmp_test_member;
CREATE TEMPORARY TABLE tmp_test_member (member_id BINARY(16) PRIMARY KEY)
SELECT member_id FROM member WHERE member_name LIKE 'tester__';

DELETE c FROM comment c JOIN post p ON p.post_id = c.post_id
WHERE p.member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM comment        WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM post           WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM schedule       WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM expense        WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM recommendation WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM payment        WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM subscription   WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM card           WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM categories     WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM device         WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM refresh_token  WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM social_account WHERE member_id IN (SELECT member_id FROM tmp_test_member);
DELETE FROM member         WHERE member_id IN (SELECT member_id FROM tmp_test_member);

DROP TEMPORARY TABLE tmp_test_member;

-- -----------------------------------------------------------------------------
-- 1. 회원 (member) 10명
--    u = 회원 번호 1~10 → UUID 끝자리 101~110
-- -----------------------------------------------------------------------------
INSERT INTO member (member_id, member_name, member_password, name, email, phone,
                    default_notify_days, notify_time, created_at)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u)),
       CONCAT('tester', LPAD(r.u, 2, '0')),
       '$2a$10$AFpw0BuK/RSLPHlT0PnwgeEYqMh5gqR0XbW9cdyQs7I.AUix/CUc2', -- qwert123!@
       r.name,
       CONCAT('tester', LPAD(r.u, 2, '0'), '@routineslim.test'),
       r.phone,
       r.notify_days,
       r.notify_time,
       NOW() - INTERVAL r.joined_days_ago DAY
FROM (
              SELECT 1 AS u, '김민지' AS name, '010-1000-0001' AS phone, 3 AS notify_days, '09:00:00' AS notify_time, 120 AS joined_days_ago
    UNION ALL SELECT 2, '이서준', '010-1000-0002', 1, '08:30:00',  95
    UNION ALL SELECT 3, '박지우', NULL,            3, '09:00:00',  80
    UNION ALL SELECT 4, '최하은', '010-1000-0004', 5, '21:00:00',  64
    UNION ALL SELECT 5, '정도윤', '010-1000-0005', 3, '12:00:00',  50
    UNION ALL SELECT 6, '강예린', NULL,            2, '07:00:00',  41
    UNION ALL SELECT 7, '조시우', '010-1000-0007', 3, '09:00:00',  30
    UNION ALL SELECT 8, '윤하린', '010-1000-0008', 7, '18:30:00',  14
    UNION ALL SELECT 9, '장도현', '010-1000-0009', 0, '10:00:00',   5
    UNION ALL SELECT 10, '임수아', NULL,           3, '09:00:00',   0
) r;

-- -----------------------------------------------------------------------------
-- 2. 소셜 계정 (social_account) — 넣지 않는다
--    서버는 소셜 계정이 연결된 회원의 아이디/비밀번호 로그인을 막는다 (AuthServiceImpl.login).
--    테스트 회원은 모두 비밀번호로 로그인해야 하므로 비워 둔다. (맨 위 0번에서 예전 행은 지운다)
-- -----------------------------------------------------------------------------

-- -----------------------------------------------------------------------------
-- 3. 로그인 세션 (refresh_token) — 지금은 1번(UUID) 방식이라 화면에는 안 쓰인다. JWT 전환 대비용
--    token_hash 는 원문이 아닌 해시만 저장 (테스트라 임의 문자열의 SHA-256)
-- -----------------------------------------------------------------------------
INSERT INTO refresh_token (member_id, token_hash, device_name, expires_at, revoked_at)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + n.u)),
       SHA2(CONCAT('test-refresh-token-', n.u), 256),
       IF(n.u % 2 = 0, 'iPhone 15', 'Galaxy S24'),
       NOW() + INTERVAL 14 DAY,
       IF(n.u = 9, NOW() - INTERVAL 1 DAY, NULL) -- tester09 는 로그아웃(폐기)된 세션
FROM (SELECT 1 AS u UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n;

-- -----------------------------------------------------------------------------
-- 4. 기기 (device) — 회원마다 1대
-- -----------------------------------------------------------------------------
INSERT INTO device (member_id, platform, fcm_token, notification_enabled, app_version, last_synced_at)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + n.u)),
       IF(n.u % 2 = 0, 'IOS', 'ANDROID'),
       CONCAT('test-fcm-token-', LPAD(n.u, 2, '0')),
       n.u <> 7, -- tester07 은 알림 끔
       '1.0.0',
       NOW() - INTERVAL n.u HOUR
FROM (SELECT 1 AS u UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n;

-- -----------------------------------------------------------------------------
-- 5. 사용자 카테고리 (categories) — 지출(expense) 분류용, 회원마다 3개
-- -----------------------------------------------------------------------------
INSERT INTO categories (categories_id, member_id, name, color, icon, sort_order)
SELECT UUID_TO_BIN(UUID()),
       UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + n.u)),
       c.name, c.color, c.icon, c.sort_order
FROM (SELECT 1 AS u UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
CROSS JOIN (
              SELECT '식비' AS name, '#F2994A' AS color, 'food' AS icon, 1 AS sort_order
    UNION ALL SELECT '교통', '#2D9CDB', 'transport', 2
    UNION ALL SELECT '쇼핑', '#E993AF', 'shopping',  3
) c;

-- -----------------------------------------------------------------------------
-- 6. 카드 (card) — 회원마다 1~2장 (tester10 은 없음)
-- -----------------------------------------------------------------------------
INSERT INTO card (member_id, card_name, payment_day)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u)),
       r.card_name,
       DATE_FORMAT(CURDATE(), CONCAT('%Y-%m-', LPAD(r.pay_day, 2, '0')))
FROM (
              SELECT 1 AS u, '현대카드 · 4821' AS card_name, 15 AS pay_day
    UNION ALL SELECT 1, '신한카드 · 1093', 25
    UNION ALL SELECT 2, '국민카드 · 3302', 14
    UNION ALL SELECT 3, '삼성카드 · 7781', 10
    UNION ALL SELECT 4, '카카오뱅크 체크카드 · 7710', 1
    UNION ALL SELECT 5, '현대카드 · 1188', 15
    UNION ALL SELECT 6, '토스뱅크 체크카드 · 0042', 1
    UNION ALL SELECT 7, '우리카드 · 5520', 21
    UNION ALL SELECT 8, '하나카드 · 6610', 12
    UNION ALL SELECT 9, '롯데카드 · 9001', 5
) r;

-- -----------------------------------------------------------------------------
-- 7. 구독 (subscription)
--    d = 오늘부터 다음 결제일까지 일 수, m = 몇 달 전에 시작했는지
--    category 는 subscription_category.category_name, 프리셋은 서비스 이름이 같으면 자동 연결
-- -----------------------------------------------------------------------------
INSERT INTO subscription (sub_id, member_id, sub_ctg_id, preset_id, card_id, name, amount, currency,
                          billing_cycle, cycle_interval, start_date, billing_day, next_billing_date,
                          status, trial_end_date, notify_enabled, notify_days_before, memo, canceled_at)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8001-0000000', 100 + r.u, LPAD(r.s, 2, '0'))),
       UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u)),
       sc.sub_ctg_id,
       sp.preset_id,
       cd.card_id,
       r.name,
       r.amount,
       'KRW',
       r.cycle,
       1,
       (CURDATE() + INTERVAL r.d DAY) - INTERVAL r.m MONTH,
       DAY(CURDATE() + INTERVAL r.d DAY),
       CURDATE() + INTERVAL r.d DAY,
       r.status,
       IF(r.status = 'TRIAL', CURDATE() + INTERVAL r.d DAY, NULL),
       r.status <> 'CANCELED',
       r.notify_days_before,
       r.memo,
       IF(r.status = 'CANCELED', NOW(3) - INTERVAL 10 DAY, NULL)
FROM (
              SELECT 1 AS u, 1 AS s, '넷플릭스' AS name, 'OTT' AS category, 17000 AS amount, 'MONTHLY' AS cycle, 1 AS d, 14 AS m, 'ACTIVE' AS status, '현대카드 · 4821' AS card, NULL AS notify_days_before, '가족 공유 프리미엄' AS memo
    UNION ALL SELECT 1, 2, 'iCloud+',         '클라우드',  1100, 'MONTHLY',  1, 30, 'ACTIVE', '현대카드 · 4821', NULL, NULL
    UNION ALL SELECT 1, 3, '스포티파이',      '음악',     10900, 'MONTHLY',  4,  8, 'ACTIVE', '신한카드 · 1093', 1,    NULL
    UNION ALL SELECT 1, 4, '디즈니+',         'OTT',       9900, 'MONTHLY',  7,  5, 'ACTIVE', '신한카드 · 1093', NULL, '마블 다 보면 해지'
    UNION ALL SELECT 1, 5, 'YouTube Premium', 'OTT',      14900, 'MONTHLY', 12, 20, 'ACTIVE', '현대카드 · 4821', NULL, NULL
    UNION ALL SELECT 1, 6, '쿠팡 와우',       '쇼핑',      7890, 'MONTHLY', 20, 11, 'ACTIVE', NULL,              NULL, NULL

    UNION ALL SELECT 2, 1, '넷플릭스',        'OTT',      17000, 'MONTHLY',  0,  6, 'ACTIVE', '국민카드 · 3302', NULL, NULL
    UNION ALL SELECT 2, 2, '왓챠',            'OTT',       7900, 'MONTHLY', 15,  0, 'TRIAL',  '국민카드 · 3302', 3,    '체험 끝나기 전에 결정'
    UNION ALL SELECT 2, 3, '멜론',            '음악',     10900, 'MONTHLY',  3,  9, 'ACTIVE', '국민카드 · 3302', NULL, NULL

    UNION ALL SELECT 3, 1, 'YouTube Premium', 'OTT',      14900, 'MONTHLY',  2, 18, 'ACTIVE', '삼성카드 · 7781', NULL, NULL
    UNION ALL SELECT 3, 2, '네이버플러스 멤버십', '쇼핑',  4900, 'MONTHLY',  9,  4, 'ACTIVE', '삼성카드 · 7781', NULL, NULL
    UNION ALL SELECT 3, 3, 'Microsoft 365',   '클라우드', 119000, 'YEARLY', 40, 12, 'ACTIVE', '삼성카드 · 7781', 7,    '연간 결제'

    UNION ALL SELECT 4, 1, '디즈니+',         'OTT',       9900, 'MONTHLY',  5,  7, 'PAUSED', '카카오뱅크 체크카드 · 7710', NULL, '잠시 멈춤'
    UNION ALL SELECT 4, 2, '스포티파이',      '음악',     10900, 'MONTHLY',  8, 10, 'ACTIVE', '카카오뱅크 체크카드 · 7710', NULL, NULL
    UNION ALL SELECT 4, 3, '티빙',            'OTT',      13900, 'MONTHLY',  1,  3, 'ACTIVE', '카카오뱅크 체크카드 · 7710', NULL, NULL

    UNION ALL SELECT 5, 1, '쿠팡 와우',       '쇼핑',      7890, 'MONTHLY',  6, 24, 'ACTIVE',   '현대카드 · 1188', NULL, NULL
    UNION ALL SELECT 5, 2, '넷플릭스',        'OTT',      17000, 'MONTHLY', 25,  9, 'CANCELED', '현대카드 · 1188', NULL, '안 봐서 해지'
    UNION ALL SELECT 5, 3, 'Google One',      '클라우드',  2400, 'MONTHLY', 11,  6, 'ACTIVE',   '현대카드 · 1188', NULL, NULL

    UNION ALL SELECT 6, 1, '주간 밀키트',     '쇼핑',     12900, 'WEEKLY',   3,  2, 'ACTIVE', '토스뱅크 체크카드 · 0042', 1,    '매주 배송'
    UNION ALL SELECT 6, 2, 'iCloud+',         '클라우드',  4400, 'MONTHLY', 14, 15, 'ACTIVE', '토스뱅크 체크카드 · 0042', NULL, '200GB'
    UNION ALL SELECT 6, 3, '스포티파이',      '음악',     10900, 'MONTHLY',  0,  4, 'ACTIVE', '토스뱅크 체크카드 · 0042', NULL, NULL

    UNION ALL SELECT 7, 1, 'YouTube Premium', 'OTT',      14900, 'MONTHLY',  1, 10, 'ACTIVE', '우리카드 · 5520', NULL, NULL
    UNION ALL SELECT 7, 2, '웨이브',          'OTT',      10900, 'MONTHLY', 18,  5, 'ACTIVE', '우리카드 · 5520', NULL, NULL
    UNION ALL SELECT 7, 3, 'iCloud+',         '클라우드',  1100, 'MONTHLY', 22, 13, 'ACTIVE', '우리카드 · 5520', NULL, NULL
    UNION ALL SELECT 7, 4, '바이브',          '음악',      8500, 'MONTHLY',  6,  2, 'ACTIVE', '우리카드 · 5520', NULL, NULL

    UNION ALL SELECT 8, 1, '넷플릭스',        'OTT',      17000, 'MONTHLY', 30,  3, 'ACTIVE', '하나카드 · 6610', NULL, NULL
    UNION ALL SELECT 8, 2, '애플 뮤직',       '음악',     11000, 'MONTHLY', 21,  0, 'TRIAL',  '하나카드 · 6610', NULL, '무료 체험 1개월'

    UNION ALL SELECT 9, 1, '쿠팡 와우',       '쇼핑',      7890, 'MONTHLY', 13,  1, 'ACTIVE', '롯데카드 · 9001', NULL, NULL
) r
JOIN subscription_category sc ON sc.category_name = r.category
LEFT JOIN service_preset sp ON sp.service_name = r.name
LEFT JOIN card cd ON cd.member_id = UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u))
                 AND cd.card_name = r.card;

-- -----------------------------------------------------------------------------
-- 8. 청구 기록 (payment) — 체험판을 뺀 구독의 지난 결제 (최대 3번, 시작일 이후만)
--    월간: 다음 결제일 1·2·3달 전 / 매주: 1·2·3주 전 / 연간: 1년 전
-- -----------------------------------------------------------------------------
INSERT INTO payment (sub_id, member_id, billed_on, amount, currency, source)
SELECT s.sub_id, s.member_id, b.billed_on, s.amount, s.currency, 'AUTO'
FROM subscription s
JOIN member m ON m.member_id = s.member_id AND m.member_name LIKE 'tester__'
CROSS JOIN (SELECT 1 AS k UNION ALL SELECT 2 UNION ALL SELECT 3) n
CROSS JOIN LATERAL (
    SELECT CASE s.billing_cycle
               WHEN 'MONTHLY' THEN s.next_billing_date - INTERVAL n.k MONTH
               WHEN 'WEEKLY'  THEN s.next_billing_date - INTERVAL n.k WEEK
               ELSE IF(n.k = 1, s.next_billing_date - INTERVAL 1 YEAR, NULL)
           END AS billed_on
) b
WHERE s.status <> 'TRIAL'
  AND b.billed_on IS NOT NULL
  AND b.billed_on >= s.start_date
  AND b.billed_on <= CURDATE();

-- -----------------------------------------------------------------------------
-- 9. 절약 추천 (recommendation)
-- -----------------------------------------------------------------------------
INSERT INTO recommendation (rec_id, sub_id, member_id, title, rule_code, saving_amount, status)
SELECT UUID_TO_BIN(UUID()),
       UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8001-0000000', 100 + r.u, LPAD(r.s, 2, '0'))),
       UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u)),
       r.title, r.rule_code, r.saving_amount, r.status
FROM (
              SELECT 1 AS u, 4 AS s, 'OTT 구독이 3개예요. 디즈니+를 정리해 보세요' AS title, 'DUPLICATE_CATEGORY' AS rule_code, 9900 AS saving_amount, 'NEW' AS status
    UNION ALL SELECT 1, 1, '넷플릭스를 광고형으로 바꾸면 아낄 수 있어요', 'CHEAPER_PLAN',   11500, 'NEW'
    UNION ALL SELECT 2, 2, '왓챠 무료 체험이 곧 끝나요',                 'TRIAL_ENDING',    7900, 'NEW'
    UNION ALL SELECT 3, 1, 'YouTube Premium 가족 요금제로 나눠 보세요',  'PARTY_SHARE',     7450, 'DISMISSED'
    UNION ALL SELECT 4, 3, 'OTT 구독이 겹쳐요. 티빙을 정리해 보세요',    'DUPLICATE_CATEGORY', 13900, 'NEW'
    UNION ALL SELECT 5, 2, '넷플릭스를 해지했어요',                      'DUPLICATE_CATEGORY', 17000, 'ACTED'
    UNION ALL SELECT 7, 2, '웨이브를 연간 결제로 바꾸면 더 저렴해요',    'YEARLY_CHEAPER',   1800, 'NEW'
) r;

-- -----------------------------------------------------------------------------
-- 10. 지출 (expense) — 회원마다 4건 (이번 달·지난달)
-- -----------------------------------------------------------------------------
INSERT INTO expense (member_id, expense_method_id, card_id, categories_id, amount, description, expense_date)
SELECT m.member_id,
       em.expense_method_id,
       IF(e.method IN ('신용카드', '체크카드'),
          (SELECT MIN(cd.card_id) FROM card cd WHERE cd.member_id = m.member_id), NULL),
       ct.categories_id,
       e.amount + n.u * 100, -- 회원마다 금액을 조금씩 다르게
       e.description,
       CURDATE() - INTERVAL (e.days_ago + n.u) DAY
FROM (SELECT 1 AS u UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
JOIN member m ON m.member_id = UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + n.u))
CROSS JOIN (
              SELECT '식비' AS category, '신용카드' AS method, 12000 AS amount, '점심 식사' AS description, 1 AS days_ago
    UNION ALL SELECT '교통', '체크카드',  1550, '지하철',     2
    UNION ALL SELECT '쇼핑', '간편결제', 34900, '생활용품',   9
    UNION ALL SELECT '식비', '현금',      8000, '카페',      25
) e
JOIN expense_method em ON em.method_name = e.method
JOIN categories ct ON ct.member_id = m.member_id AND ct.name = e.category;

-- -----------------------------------------------------------------------------
-- 11. 결제 일정 (schedule) — 회원마다 2건
-- -----------------------------------------------------------------------------
INSERT INTO schedule (member_id, title, amount, category, payment_date, description, repeat_type, payment_status, end_date)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + n.u)),
       s.title,
       s.amount,
       s.category,
       TIMESTAMP(CURDATE() + INTERVAL (s.in_days + n.u) DAY, '09:00:00'),
       s.description,
       s.repeat_type,
       s.payment_status,
       s.end_date
FROM (SELECT 1 AS u UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5
      UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9 UNION ALL SELECT 10) n
CROSS JOIN (
              SELECT '월세' AS title, 550000 AS amount, '고정비' AS category, 3 AS in_days, '자동이체' AS description, 'MONTHLY' AS repeat_type, 'PENDING' AS payment_status, NULL AS end_date
    UNION ALL SELECT '카드 대금', 320000, '카드 결제', -2, NULL, 'MONTHLY', 'COMPLETED', CURDATE() + INTERVAL 1 YEAR
) s;

-- -----------------------------------------------------------------------------
-- 12. 커뮤니티 게시글 (post) / 댓글 (comment)  ※ Flyway V6 이후 (게시판·파티 정보·조회수·댓글 작성자)
--     board: PARTY 파티원 모집 / REVIEW 후기 / FREE 자유. 파티 정보는 PARTY 글만 넣는다
-- -----------------------------------------------------------------------------
INSERT INTO post (member_id, board, title, content, service_name, recruit_status, current_members, max_members,
                  total_price, price_per_member, settlement_day, view_count, created_at)
SELECT UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + r.u)), r.board, r.title, r.content,
       r.service_name, IF(r.board = 'PARTY', r.recruit_status, NULL), r.current_members, r.max_members,
       r.total_price, r.price_per_member, r.settlement_day, r.view_count,
       NOW() - INTERVAL r.hours_ago HOUR
FROM (
              SELECT 1 AS u, 'PARTY' AS board, '넷플릭스 프리미엄 파티원 3명 구해요' AS title, '1인당 4,250원, 매월 15일 정산합니다. 오래 함께하실 분 환영해요.' AS content, '넷플릭스' AS service_name, 'RECRUITING' AS recruit_status, 1 AS current_members, 4 AS max_members, 17000 AS total_price, 4250 AS price_per_member, 15 AS settlement_day, 24 AS view_count, 2 AS hours_ago
    UNION ALL SELECT 2, 'REVIEW', '왓챠 체험판 써 본 후기', '영화 위주라면 괜찮은데 드라마는 아쉬워요.', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 8, 5
    UNION ALL SELECT 3, 'PARTY', 'YouTube Premium 가족 요금제 같이 하실 분', '5명 모집, 1인당 2,980원이에요.', 'YouTube Premium', 'RECRUITING', 2, 6, 17900, 2980, 1, 15, 9
    UNION ALL SELECT 4, 'FREE', '디즈니+ 일시정지 기능 써 보셨나요?', '한 달 쉬었다가 다시 켜도 기록이 남는지 궁금해요.', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 5, 20
    UNION ALL SELECT 5, 'REVIEW', '넷플릭스 해지하고 한 달 지났어요', '생각보다 안 아쉽네요. 월 17,000원 아끼는 중!', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 31, 26
    UNION ALL SELECT 6, 'FREE', '구독 정리 팁 공유합니다', '결제일 3일 전 알림 켜 두면 자동 결제 전에 한 번 더 생각하게 돼요.', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 40, 30
    UNION ALL SELECT 7, 'FREE', '음악 앱 뭐 쓰세요?', '바이브랑 스포티파이 고민 중이에요.', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 12, 48
    UNION ALL SELECT 8, 'REVIEW', '애플 뮤직 무료 체험 중', '체험 끝나기 전에 알림 오는지 확인해 볼게요.', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3, 60
    UNION ALL SELECT 9, 'PARTY', '쿠팡 와우 같이 쓰실 분 (마감)', '인원 다 찼습니다. 감사합니다!', '쿠팡 와우', 'CLOSED', 2, 2, 7890, 3945, 25, 19, 72
    UNION ALL SELECT 10, 'FREE', '루틴슬림 처음 써 봐요', '아직 등록한 구독이 없어요. 뭐부터 넣으면 좋을까요?', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 2, 1
) r;

-- u: 댓글 작성자 (tester01~10)
INSERT INTO comment (post_id, member_id, content, created_at)
SELECT p.post_id, UUID_TO_BIN(CONCAT('019a3c2e-0000-7000-8000-000000000', 100 + c.u)), c.content,
       p.created_at + INTERVAL c.minutes_after MINUTE
FROM post p
JOIN member m ON m.member_id = p.member_id AND m.member_name LIKE 'tester__'
JOIN (
              SELECT '넷플릭스 프리미엄 파티원 3명 구해요' AS title, 3 AS u, '저 참여할게요!' AS content, 10 AS minutes_after
    UNION ALL SELECT '넷플릭스 프리미엄 파티원 3명 구해요', 7, '정산은 계좌이체인가요?', 35
    UNION ALL SELECT 'YouTube Premium 가족 요금제 같이 하실 분', 5, '자리 남았나요?', 15
    UNION ALL SELECT '구독 정리 팁 공유합니다', 2, '좋은 팁 감사합니다', 60
    UNION ALL SELECT '음악 앱 뭐 쓰세요?', 8, '스포티파이 추천해요. 추천 알고리즘이 좋아요.', 20
    UNION ALL SELECT '음악 앱 뭐 쓰세요?', 4, '바이브 쓰는데 국내 음악은 괜찮아요.', 45
    UNION ALL SELECT '루틴슬림 처음 써 봐요', 1, '매달 나가는 것부터 넣어 보세요!', 5
) c ON c.title = p.title;

-- -----------------------------------------------------------------------------
-- 확인
-- -----------------------------------------------------------------------------
SELECT m.member_name, m.name,
       (SELECT COUNT(*) FROM subscription s WHERE s.member_id = m.member_id) AS subscriptions,
       (SELECT COUNT(*) FROM payment p WHERE p.member_id = m.member_id)      AS payments,
       (SELECT COUNT(*) FROM card c WHERE c.member_id = m.member_id)         AS cards,
       (SELECT COUNT(*) FROM expense e WHERE e.member_id = m.member_id)      AS expenses,
       (SELECT COUNT(*) FROM post p WHERE p.member_id = m.member_id)         AS posts
FROM member m
WHERE m.member_name LIKE 'tester__'
ORDER BY m.member_name;
