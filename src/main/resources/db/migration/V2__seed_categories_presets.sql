-- 기본 데이터: 구독 카테고리, 인기 서비스 프리셋, 결제 수단 종류
-- 카테고리와 색은 앱(src/features/subscription/categories.ts, theme/colors.ts)과 맞춘다.
-- 프리셋 요금은 앱 Figma 시안 값이다. 실제 요금은 프리셋 데이터 담당이 확인해 V3 이후 마이그레이션으로 고친다.

INSERT INTO subscription_category (category_name, color, icon, sort_order) VALUES
    ('OTT',      '#5B5BD6', 'ott',      1),
    ('음악',     '#9393E8', 'music',    2),
    ('클라우드', '#BDC1D4', 'cloud',    3),
    ('쇼핑',     '#E993AF', 'shopping', 4);

INSERT INTO service_preset (sub_ctg_id, service_name, default_price, billing_cycle)
SELECT c.sub_ctg_id, p.service_name, p.default_price, 'MONTHLY'
FROM (
    SELECT 'OTT' AS category_name, '넷플릭스' AS service_name, 17000 AS default_price
    UNION ALL SELECT 'OTT',      '디즈니+',         9900
    UNION ALL SELECT 'OTT',      'YouTube Premium', 14900
    UNION ALL SELECT '음악',     '스포티파이',      10900
    UNION ALL SELECT '클라우드', 'iCloud+',         1100
    UNION ALL SELECT '쇼핑',     '쿠팡 와우',       7890
) p
JOIN subscription_category c ON c.category_name = p.category_name;

INSERT INTO expense_method (method_name) VALUES
    ('신용카드'),
    ('체크카드'),
    ('계좌이체'),
    ('간편결제'),
    ('현금');
