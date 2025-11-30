-- ============================================================
-- TPT-API 부하테스트용 FeedbackRequest 더미 데이터 생성 SQL
--
-- 사용 방법:
-- 1. create-test-accounts.sql 실행 후 진행
-- 2. 이 스크립트 실행하여 5000개의 피드백 요청 생성
--
-- 규칙:
-- - DAY 타입만 (SWING 제외)
-- - BEFORE_COMPLETION 완강 상태만
-- - 날짜: investment_history.started_at ~ 2025-11-28
-- - PnL: 다양하게 분포 (-50% ~ +100%)
-- - LONG 포지션: PnL > 0 → exitPrice > entryPrice
-- - 완강 전 필수 필드: positionStartReason, positionEndReason
--
-- ⚠️ 주의: 운영 DB에서 실행하므로 반드시 백업 후 진행!
-- ============================================================

-- 테스트 계정 시작 번호 (create-test-accounts.sql과 일치)
SET @START_USER_NUM = 900001;
SET @TODAY = '2025-11-28';

-- ============================================================
-- Step 1: 테스트 계정용 investment_history 생성
-- 회원가입 시점을 시뮬레이션 (6개월 전 ~ 3개월 전 사이 랜덤)
-- ============================================================
DELIMITER //

DROP PROCEDURE IF EXISTS create_investment_histories //

CREATE PROCEDURE create_investment_histories()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE user_id BIGINT;
    DECLARE random_days INT;
    DECLARE start_date DATE;

    START TRANSACTION;

    -- 기존 테스트 계정의 investment_history 삭제
    DELETE ih FROM investment_history ih
    JOIN customer c ON ih.customer_id = c.user_id
    JOIN user u ON c.user_id = u.user_id
    WHERE u.username LIKE 'loadtest_user_%';

    WHILE i < 1000 DO
        -- 테스트 계정 user_id 찾기
        SELECT u.user_id INTO user_id
        FROM user u
        WHERE u.username = CONCAT('loadtest_user_', @START_USER_NUM + i)
        LIMIT 1;

        IF user_id IS NOT NULL THEN
            -- 6개월 전 ~ 3개월 전 사이 랜덤 날짜 (90 ~ 180일 전)
            SET random_days = FLOOR(90 + RAND() * 90);
            SET start_date = DATE_SUB(@TODAY, INTERVAL random_days DAY);

            INSERT INTO investment_history (
                customer_id,
                investment_type,
                started_at,
                ended_at,
                created_at,
                updated_at
            ) VALUES (
                user_id,
                'DAY',
                start_date,
                NULL,  -- 진행 중
                NOW(),
                NOW()
            );
        END IF;

        SET i = i + 1;

        IF i MOD 100 = 0 THEN
            SELECT CONCAT('Created ', i, ' investment histories...') AS progress;
        END IF;
    END WHILE;

    COMMIT;
    SELECT CONCAT('Successfully created investment histories for ', i, ' test accounts!') AS result;
END //

DELIMITER ;

-- 실행
CALL create_investment_histories();

-- ============================================================
-- Step 2: FeedbackRequest 더미 데이터 5000개 생성
-- ============================================================
DELIMITER //

DROP PROCEDURE IF EXISTS create_feedback_requests //

CREATE PROCEDURE create_feedback_requests(IN total_records INT)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE user_id BIGINT;
    DECLARE inv_start_date DATE;
    DECLARE feedback_date DATE;
    DECLARE random_user_idx INT;
    DECLARE days_diff INT;
    DECLARE random_day_offset INT;

    -- 피드백 필드들
    DECLARE v_pnl DECIMAL(19,2);
    DECLARE v_total_asset_pnl DECIMAL(19,2);
    DECLARE v_entry_price DECIMAL(19,2);
    DECLARE v_exit_price DECIMAL(19,2);
    DECLARE v_position VARCHAR(10);
    DECLARE v_operating_funds_ratio INT;
    DECLARE v_leverage DECIMAL(5,2);
    DECLARE v_risk_taking DECIMAL(19,2);
    DECLARE v_rnr DOUBLE;
    DECLARE v_setting_stop_loss DECIMAL(19,2);
    DECLARE v_setting_take_profit DECIMAL(19,2);
    DECLARE v_category VARCHAR(50);
    DECLARE v_title VARCHAR(255);
    DECLARE v_feedback_year INT;
    DECLARE v_feedback_month INT;
    DECLARE v_feedback_week INT;
    DECLARE v_holding_time VARCHAR(50);

    -- 카테고리 옵션
    DECLARE categories VARCHAR(500) DEFAULT 'BTC,ETH,XRP,SOL,DOGE,ADA,AVAX,MATIC,DOT,LINK,ATOM,UNI,FTM,NEAR,APT,삼성전자,SK하이닉스,NAVER,카카오,LG에너지솔루션,현대차,기아,셀트리온,삼성바이오로직스,KB금융';
    DECLARE category_count INT DEFAULT 25;

    -- 홀딩 시간 옵션
    DECLARE holding_times VARCHAR(200) DEFAULT '5분,10분,15분,30분,1시간,2시간,3시간,4시간,6시간,8시간';
    DECLARE holding_count INT DEFAULT 10;

    START TRANSACTION;

    WHILE i < total_records DO
        -- 랜덤 사용자 선택 (0-999 중)
        SET random_user_idx = FLOOR(RAND() * 1000);

        -- 해당 사용자의 investment_history 시작일 조회
        SELECT ih.customer_id, ih.started_at
        INTO user_id, inv_start_date
        FROM investment_history ih
        JOIN user u ON ih.customer_id = u.user_id
        WHERE u.username = CONCAT('loadtest_user_', @START_USER_NUM + random_user_idx)
        LIMIT 1;

        IF user_id IS NOT NULL AND inv_start_date IS NOT NULL THEN
            -- investment_history 시작일 ~ 오늘 사이 랜덤 날짜
            SET days_diff = DATEDIFF(@TODAY, inv_start_date);
            IF days_diff > 0 THEN
                SET random_day_offset = FLOOR(RAND() * days_diff);
                SET feedback_date = DATE_ADD(inv_start_date, INTERVAL random_day_offset DAY);
            ELSE
                SET feedback_date = inv_start_date;
            END IF;

            -- 연/월/주차 계산
            SET v_feedback_year = YEAR(feedback_date);
            SET v_feedback_month = MONTH(feedback_date);
            SET v_feedback_week = CEIL(DAY(feedback_date) / 7);
            IF v_feedback_week > 5 THEN SET v_feedback_week = 5; END IF;

            -- 랜덤 카테고리 선택
            SET v_category = SUBSTRING_INDEX(SUBSTRING_INDEX(categories, ',', FLOOR(1 + RAND() * category_count)), ',', -1);

            -- 랜덤 홀딩 시간
            SET v_holding_time = SUBSTRING_INDEX(SUBSTRING_INDEX(holding_times, ',', FLOOR(1 + RAND() * holding_count)), ',', -1);

            -- 포지션 랜덤 (LONG 70%, SHORT 30%)
            IF RAND() < 0.7 THEN
                SET v_position = 'LONG';
            ELSE
                SET v_position = 'SHORT';
            END IF;

            -- 비중 (운용 자금 대비) 1-100%
            SET v_operating_funds_ratio = FLOOR(5 + RAND() * 95);

            -- 레버리지 1-20배 (대부분 낮은 레버리지)
            SET v_leverage = ROUND(1 + RAND() * RAND() * 19, 2);

            -- PnL 생성 (-50% ~ +100%, 정규분포 비슷하게)
            -- 대부분 -20% ~ +30% 사이, 가끔 극단값
            SET v_pnl = ROUND((RAND() + RAND() + RAND() - 1.5) * 50, 2);

            -- totalAssetPnl = pnl * operatingFundsRatio / 100
            SET v_total_asset_pnl = ROUND(v_pnl * v_operating_funds_ratio / 100, 2);

            -- 진입가격 (10000 ~ 100000 사이 랜덤)
            SET v_entry_price = ROUND(10000 + RAND() * 90000, 2);

            -- 탈출가격 계산 (PnL과 포지션에 따라 논리적으로)
            IF v_position = 'LONG' THEN
                -- LONG: PnL > 0이면 exit > entry, PnL < 0이면 exit < entry
                SET v_exit_price = ROUND(v_entry_price * (1 + v_pnl / 100), 2);
            ELSE
                -- SHORT: PnL > 0이면 exit < entry, PnL < 0이면 exit > entry
                SET v_exit_price = ROUND(v_entry_price * (1 - v_pnl / 100), 2);
            END IF;

            -- 리스크 테이킹 (1-10%)
            SET v_risk_taking = ROUND(1 + RAND() * 9, 2);

            -- 손절가 계산 (진입가 기준 -1% ~ -10%)
            IF v_position = 'LONG' THEN
                SET v_setting_stop_loss = ROUND(v_entry_price * (1 - v_risk_taking / 100), 2);
            ELSE
                SET v_setting_stop_loss = ROUND(v_entry_price * (1 + v_risk_taking / 100), 2);
            END IF;

            -- 익절가 (진입가 기준 +5% ~ +30%)
            IF v_position = 'LONG' THEN
                SET v_setting_take_profit = ROUND(v_entry_price * (1 + (5 + RAND() * 25) / 100), 2);
            ELSE
                SET v_setting_take_profit = ROUND(v_entry_price * (1 - (5 + RAND() * 25) / 100), 2);
            END IF;

            -- R&R 계산 (손익비)
            SET v_rnr = ROUND(ABS(v_pnl) / v_risk_taking, 2);
            IF v_rnr > 10 THEN SET v_rnr = ROUND(1 + RAND() * 5, 2); END IF;

            -- 제목 생성: "월/일 종목 ±전체자산대비PnL%"
            SET v_title = CONCAT(
                MONTH(feedback_date), '/', DAY(feedback_date), ' ',
                v_category, ' ',
                IF(v_total_asset_pnl >= 0, '+', ''),
                v_total_asset_pnl, '%'
            );

            -- INSERT
            INSERT INTO feedback_request (
                -- 기본 정보
                customer_id,
                investment_type,
                course_status,
                membership_level,
                status,

                -- 날짜 정보
                feedback_request_date,
                feedback_year,
                feedback_month,
                feedback_week,

                -- 매매 정보
                title,
                category,
                position,
                position_holding_time,

                -- 가격 정보
                entry_price,
                exit_price,
                setting_stop_loss,
                setting_take_profit,

                -- 비율 정보
                pnl,
                total_asset_pnl,
                rnr,
                operating_funds_ratio,
                leverage,
                risk_taking,

                -- 완강 전 필수 필드
                position_start_reason,
                position_end_reason,
                trading_review,

                -- 플래그
                is_best_feedback,
                is_read,
                is_responded,
                is_token_used,
                is_trainer_written,

                -- 타임스탬프
                created_at,
                updated_at
            ) VALUES (
                user_id,
                'DAY',
                'BEFORE_COMPLETION',
                'BASIC',
                'N',

                feedback_date,
                v_feedback_year,
                v_feedback_month,
                v_feedback_week,

                v_title,
                v_category,
                v_position,
                v_holding_time,

                v_entry_price,
                v_exit_price,
                v_setting_stop_loss,
                v_setting_take_profit,

                v_pnl,
                v_total_asset_pnl,
                v_rnr,
                v_operating_funds_ratio,
                v_leverage,
                v_risk_taking,

                -- 완강 전 필수 필드 (테스트 데이터)
                CONCAT('진입 근거: ', v_category, ' 차트 분석 결과 ', v_position, ' 포지션 진입 적합. 지지/저항 확인 후 ', v_risk_taking, '% 리스크로 진입.'),
                CONCAT('탈출 근거: 목표가 도달 또는 손절가 터치. 최종 P&L ', v_pnl, '%. 홀딩 시간 ', v_holding_time, '.'),
                CONCAT('매매 복기: 이번 트레이드는 ', IF(v_pnl >= 0, '수익', '손실'), ' 거래였습니다. ',
                       v_category, ' ', v_position, ' 포지션으로 ', v_holding_time, ' 홀딩 후 ',
                       v_pnl, '% P&L 기록. 레버리지 ', v_leverage, '배, 비중 ', v_operating_funds_ratio, '% 적용.'),

                0,  -- is_best_feedback
                0,  -- is_read
                0,  -- is_responded
                0,  -- is_token_used
                0,  -- is_trainer_written

                DATE_ADD(feedback_date, INTERVAL FLOOR(RAND() * 12) HOUR),
                DATE_ADD(feedback_date, INTERVAL FLOOR(RAND() * 12) HOUR)
            );
        END IF;

        SET i = i + 1;

        IF i MOD 500 = 0 THEN
            SELECT CONCAT('Created ', i, ' feedback requests...') AS progress;
        END IF;

    END WHILE;

    COMMIT;
    SELECT CONCAT('Successfully created ', total_records, ' feedback requests!') AS result;
END //

DELIMITER ;

-- ============================================================
-- 실행: 5000개 피드백 요청 생성
-- ============================================================
CALL create_feedback_requests(5000);

-- 생성된 데이터 확인
SELECT
    COUNT(*) as total_feedback_requests,
    MIN(feedback_request_date) as earliest_date,
    MAX(feedback_request_date) as latest_date,
    AVG(pnl) as avg_pnl,
    COUNT(CASE WHEN pnl > 0 THEN 1 END) as profitable_count,
    COUNT(CASE WHEN pnl < 0 THEN 1 END) as loss_count,
    COUNT(CASE WHEN pnl = 0 THEN 1 END) as breakeven_count
FROM feedback_request fr
JOIN user u ON fr.customer_id = u.user_id
WHERE u.username LIKE 'loadtest_user_%';

-- 월별 분포 확인
SELECT
    feedback_year,
    feedback_month,
    COUNT(*) as count,
    ROUND(AVG(pnl), 2) as avg_pnl
FROM feedback_request fr
JOIN user u ON fr.customer_id = u.user_id
WHERE u.username LIKE 'loadtest_user_%'
GROUP BY feedback_year, feedback_month
ORDER BY feedback_year, feedback_month;

-- 샘플 데이터 확인
SELECT
    fr.feedback_request_id,
    u.username,
    fr.feedback_request_date,
    fr.title,
    fr.category,
    fr.position,
    fr.pnl,
    fr.total_asset_pnl,
    fr.entry_price,
    fr.exit_price
FROM feedback_request fr
JOIN user u ON fr.customer_id = u.user_id
WHERE u.username LIKE 'loadtest_user_%'
LIMIT 10;
