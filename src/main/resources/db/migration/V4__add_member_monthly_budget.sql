-- 월 예산 (앱: 마이 > 월 예산). NULL 이면 미설정
ALTER TABLE member
    ADD COLUMN monthly_budget INT UNSIGNED NULL COMMENT '월 예산 (원), NULL 이면 미설정' AFTER notify_time;
