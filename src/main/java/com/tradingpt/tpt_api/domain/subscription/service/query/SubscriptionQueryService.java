package com.tradingpt.tpt_api.domain.subscription.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerResponseDTO;

/**
 * 구독 조회 서비스 인터페이스
 * 구독 관련 읽기 전용 작업 정의 (CQRS 패턴)
 */
public interface SubscriptionQueryService {

    /**
     * 활성 구독 고객 목록 조회
     *
     * @param trainerId 트레이너 ID (본인 담당 고객만 조회)
     * @param myCustomersOnly true: 내 담당 고객만, false/null: 전체 고객
     * @param pageable 페이징 정보
     * @return 구독 고객 슬라이스
     */
    Slice<SubscriptionCustomerResponseDTO> getActiveSubscriptionCustomers(
        Long trainerId,
        Boolean myCustomersOnly,
        Pageable pageable
    );
}
