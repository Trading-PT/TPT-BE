-- ============================================================
-- TPT-API 부하테스트용 테스트 계정 생성 SQL
--
-- 사용 방법:
-- 1. 운영 DB에 직접 연결
-- 2. 이 스크립트 실행하여 1000개의 테스트 계정 생성
-- 3. k6로 부하테스트 실행
-- 4. 테스트 완료 후 cleanup-test-accounts.sql 실행
--
-- ⚠️ 주의: 운영 DB에서 실행하므로 반드시 백업 후 진행!
-- ============================================================

-- BCrypt 해시된 비밀번호 (원본: "loadtest123!")
-- Spring Security BCryptPasswordEncoder로 생성된 해시
-- 2025-11-30: BCrypt 해시 수정 (기존 해시가 비밀번호와 매치되지 않는 문제 해결)
SET @BCRYPT_PASSWORD = '$2a$10$X98mIKlfacwSsFTLet.ReOsdbBpGSUddcHWG91OlaPSm8L1kWCC6y';

-- 테스트 계정 시작 번호 (기존 데이터와 충돌 방지)
SET @START_NUM = 900001;

-- ============================================================
-- 프로시저: 테스트 계정 벌크 생성
-- ============================================================
DELIMITER //

DROP PROCEDURE IF EXISTS create_loadtest_accounts //

CREATE PROCEDURE create_loadtest_accounts(IN num_accounts INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE current_user_id BIGINT;
    DECLARE account_username VARCHAR(255);
    DECLARE account_email VARCHAR(255);
    DECLARE account_name VARCHAR(255);

    -- 트랜잭션 시작
    START TRANSACTION;

    WHILE i < num_accounts DO
        SET account_username = CONCAT('loadtest_user_', @START_NUM + i);
        SET account_email = CONCAT('loadtest_', @START_NUM + i, '@test.tradingpt.kr');
        SET account_name = CONCAT('부하테스트사용자', @START_NUM + i);

        -- 1. User 테이블에 기본 정보 삽입 (JOINED 상속의 부모 테이블)
        INSERT INTO user (
            username,
            email,
            password,
            name,
            provider,
            provider_id,
            nickname,
            version,
            role,
            created_at,
            updated_at
        ) VALUES (
            account_username,
            account_email,
            @BCRYPT_PASSWORD,
            account_name,
            'LOCAL',
            NULL,
            account_username,
            0,
            'ROLE_CUSTOMER',
            NOW(),
            NOW()
        );

        -- 방금 생성된 user_id 가져오기
        SET current_user_id = LAST_INSERT_ID();

        -- 2. Customer 테이블에 상세 정보 삽입 (JOINED 상속의 자식 테이블)
        INSERT INTO customer (
            user_id,
            phone_number,
            primary_investment_type,
            status,
            user_status,
            membership_level,
            membership_expired_at,
            course_status,
            open_chapter_number,
            token,
            feedback_request_count,
            trainer_id
        ) VALUES (
            current_user_id,
            CONCAT('010', LPAD(@START_NUM + i, 8, '0')),  -- 01090000001 ~ 형태
            'DAY',                                         -- 기본 투자 유형
            'ACTIVE',                                      -- 계정 활성화 상태
            'UID_APPROVED',                               -- UID 승인 완료 (부하테스트용)
            'BASIC',                                       -- 기본 멤버십
            NULL,                                          -- 멤버십 만료일 없음
            'BEFORE_COMPLETION',                          -- 완강 전
            1,                                             -- 오픈된 챕터
            10,                                            -- 초기 토큰 10개
            0,                                             -- 피드백 요청 횟수
            NULL                                           -- 트레이너 미배정
        );

        SET i = i + 1;

        -- 100개마다 진행상황 로그
        IF i MOD 100 = 0 THEN
            SELECT CONCAT('Created ', i, ' accounts...') AS progress;
        END IF;

    END WHILE;

    -- 트랜잭션 커밋
    COMMIT;

    SELECT CONCAT('Successfully created ', num_accounts, ' test accounts!') AS result;
END //

DELIMITER ;

-- ============================================================
-- 실행: 1000개 테스트 계정 생성
-- ============================================================
CALL create_loadtest_accounts(1000);

-- 생성된 계정 확인
SELECT
    COUNT(*) as total_test_accounts,
    MIN(u.username) as first_account,
    MAX(u.username) as last_account
FROM user u
WHERE u.username LIKE 'loadtest_user_%';

-- 샘플 데이터 확인
SELECT
    u.user_id,
    u.username,
    u.email,
    u.name,
    c.status,
    c.user_status,
    c.membership_level,
    c.token
FROM user u
JOIN customer c ON u.user_id = c.user_id
WHERE u.username LIKE 'loadtest_user_%'
LIMIT 5;
