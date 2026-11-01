-- [JWT 로그인 - 이대수] 관리자 계정 (아이디 admin / 비밀번호 qwert123!@)
-- - member_id 는 BINARY(16) UUID 이다. 고정값 019a3c2e-0000-7000-8000-000000000001 을 16진수 리터럴(X'...')로 넣는다.
-- - 비밀번호는 BCrypt 해시로만 저장한다 (PasswordEncoderConfig 의 BCryptPasswordEncoder 와 같은 방식).
-- - 실제 서비스 공개 전에 비밀번호를 바꿀 것: 새 해시로 UPDATE 하는 V 파일을 추가한다 (이 파일은 고치지 않는다).
INSERT INTO member (member_id, member_name, member_password, name)
VALUES (X'019A3C2E000070008000000000000001',
        'admin',
        '$2a$10$AFpw0BuK/RSLPHlT0PnwgeEYqMh5gqR0XbW9cdyQs7I.AUix/CUc2',
        '관리자');
