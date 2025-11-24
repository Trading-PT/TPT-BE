-- =====================================================
-- JPA Optimistic Locking을 위한 version 컬럼 추가
-- Feature: 토큰 보상 동시성 제어
--
-- 목적:
-- - 동시 요청 시 데이터 정합성 보장
-- - 토큰 중복 지급 방지
-- - 낙관적 락을 통한 성능 최적화
--
-- 실제 발생 확률: 거의 0% (1인 1접근 패턴)
-- 방어적 프로그래밍: 예상치 못한 네트워크 재시도, 브라우저 중복 요청 대비
-- =====================================================

-- customer 테이블에 version 컬럼 추가
ALTER TABLE customer
ADD COLUMN version BIGINT DEFAULT 0 NOT NULL COMMENT 'JPA Optimistic Locking 버전 (동시성 제어)';

-- 기존 데이터에 대해 version 초기화 (0으로 설정)
-- 이미 DEFAULT 0이 설정되어 있어 자동으로 0으로 초기화됨

-- 인덱스는 불필요 (PK인 user_id로 조회하므로)
