package com.tradingpt.tpt_api.domain.subscription.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerResponseDTO;

/**
 * Subscription 커스텀 리포지토리 인터페이스
 * QueryDSL을 활용한 복잡한 조회 쿼리 정의
 */
public interface SubscriptionRepositoryCustom {

    /**
     * 활성 구독 고객 목록 조회 (슬라이스 방식)
     *
     * @param trainerId 트레이너 ID (null이면 전체 조회)
     * @param pageable 페이징 정보
     * @return 구독 고객 슬라이스
     */
    Slice<SubscriptionCustomerResponseDTO> findActiveSubscriptionCustomers(Long trainerId, Pageable pageable);
}
