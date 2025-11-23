-- =====================================================
-- 피드백 카운트 필드 추가 및 초기 데이터 동기화
-- Feature: 매매일지 n개 작성 시 토큰 m개 발급
--
-- 누적 작성 횟수 개념 (단조증가):
-- - 작성 시: 카운트 증가
-- - 삭제 시: 카운트 감소 안 함 (총 몇 개를 작성했는지만 카운트)
-- - N개마다 M개 토큰 지급 (예: 5개마다 3개 토큰)
-- =====================================================

-- 1. 컬럼 추가 (NULL 허용 상태로 먼저 추가)
ALTER TABLE customer
ADD COLUMN feedback_request_count INT DEFAULT 0 COMMENT '피드백 요청 누적 작성 횟수 (단조증가, 토큰 보상 기준)';

-- 2. 기존 데이터 동기화
-- 각 고객의 실제 피드백 요청 개수를 계산하여 설정
UPDATE customer c
SET c.feedback_request_count = (
    SELECT COUNT(*)
    FROM feedback_request fr
    WHERE fr.customer_id = c.user_id
)
WHERE EXISTS (
    SELECT 1
    FROM feedback_request fr
    WHERE fr.customer_id = c.user_id
);

-- 3. NULL 값을 0으로 업데이트 (피드백이 없는 고객)
UPDATE customer
SET feedback_request_count = 0
WHERE feedback_request_count IS NULL;

-- 4. NOT NULL 제약 조건 추가
ALTER TABLE customer
MODIFY COLUMN feedback_request_count INT NOT NULL DEFAULT 0;

-- 5. 인덱스 추가 (선택적, 통계 조회 시 성능 향상)
-- 피드백 카운트 기반 통계나 정렬이 필요한 경우 유용
CREATE INDEX idx_customer_feedback_count
ON customer(feedback_request_count);

-- =====================================================
-- 검증 쿼리 (수동 실행용 - 주석 처리)
-- =====================================================

-- 데이터 정합성 검증
-- SELECT
--     c.user_id,
--     c.feedback_request_count AS stored_count,
--     COUNT(fr.feedback_request_id) AS actual_count,
--     CASE
--         WHEN c.feedback_request_count = COUNT(fr.feedback_request_id)
--         THEN 'OK'
--         ELSE 'MISMATCH'
--     END AS status
-- FROM customer c
-- LEFT JOIN feedback_request fr ON fr.customer_id = c.user_id
-- GROUP BY c.user_id, c.feedback_request_count
-- HAVING status = 'MISMATCH';  -- 불일치 데이터만 출력

-- 통계 확인
-- SELECT
--     COUNT(*) AS total_customers,
--     SUM(feedback_request_count) AS total_feedbacks,
--     AVG(feedback_request_count) AS avg_feedbacks_per_customer,
--     MAX(feedback_request_count) AS max_feedbacks
-- FROM customer;
