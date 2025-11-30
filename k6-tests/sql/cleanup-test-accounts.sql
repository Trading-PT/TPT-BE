-- ============================================================
-- TPT-API 부하테스트용 테스트 계정 정리 SQL
--
-- 사용 방법:
-- 부하테스트 완료 후 이 스크립트 실행하여 테스트 데이터 정리
--
-- ⚠️ 주의:
-- - 반드시 테스트 완료 후에만 실행
-- - loadtest_user_ 프리픽스로 시작하는 모든 계정 삭제됨
-- ============================================================

-- ============================================================
-- 삭제 전 확인
-- ============================================================
SELECT
    '삭제 예정 계정 수' as description,
    COUNT(*) as count
FROM user
WHERE username LIKE 'loadtest_user_%';

-- 관련 데이터 확인
SELECT
    '관련 Customer 데이터' as description,
    COUNT(*) as count
FROM customer c
JOIN user u ON c.user_id = u.user_id
WHERE u.username LIKE 'loadtest_user_%';

-- ============================================================
-- 프로시저: 테스트 계정 및 관련 데이터 삭제
-- ============================================================
DELIMITER //

DROP PROCEDURE IF EXISTS cleanup_loadtest_accounts //

CREATE PROCEDURE cleanup_loadtest_accounts()
BEGIN
    DECLARE affected_rows INT DEFAULT 0;

    -- 트랜잭션 시작
    START TRANSACTION;

    -- 1. 테스트 사용자의 피드백 요청 삭제 (있는 경우)
    DELETE fr FROM feedback_request fr
    JOIN customer c ON fr.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' feedback requests') AS progress;

    -- 2. 테스트 사용자의 메모 삭제 (있는 경우)
    DELETE m FROM memo m
    JOIN customer c ON m.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' memos') AS progress;

    -- 3. 테스트 사용자의 투자 유형 히스토리 삭제 (있는 경우)
    DELETE ih FROM investment_history ih
    JOIN customer c ON ih.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' investment histories') AS progress;

    -- 4. 테스트 사용자의 결제 수단 삭제 (있는 경우)
    DELETE pm FROM payment_method pm
    JOIN customer c ON pm.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' payment methods') AS progress;

    -- 5. 테스트 사용자의 구독 삭제 (있는 경우)
    DELETE s FROM subscription s
    JOIN customer c ON s.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' subscriptions') AS progress;

    -- 6. 테스트 사용자의 UID 삭제 (있는 경우)
    DELETE uid FROM uid uid
    JOIN customer c ON uid.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' UIDs') AS progress;

    -- 7. Customer 테이블에서 삭제 (JOINED 상속 자식)
    DELETE c FROM customer c
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' customer records') AS progress;

    -- 8. User 테이블에서 삭제 (JOINED 상속 부모)
    DELETE FROM user
    WHERE username LIKE 'loadtest_user_%';

    SELECT ROW_COUNT() INTO affected_rows;
    SELECT CONCAT('Deleted ', affected_rows, ' user records') AS progress;

    -- 트랜잭션 커밋
    COMMIT;

    SELECT 'Cleanup completed successfully!' AS result;
END //

DELIMITER ;

-- ============================================================
-- 실행: 테스트 계정 정리
-- ============================================================
-- ⚠️ 아래 주석 해제하여 실행
-- CALL cleanup_loadtest_accounts();

-- 정리 후 확인
SELECT
    'Remaining test accounts' as description,
    COUNT(*) as count
FROM user
WHERE username LIKE 'loadtest_user_%';
