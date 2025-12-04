package com.tradingpt.tpt_api.domain.subscription.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerSliceResponseDTO;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;

import lombok.RequiredArgsConstructor;

/**
 * 구독 조회 서비스 구현체
 * 구독 관련 읽기 전용 작업 구현 (CQRS 패턴)
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    /**
     * 활성 구독 고객 목록 조회
     *
     * 비즈니스 로직:
     * - myCustomersOnly = true: 트레이너 본인 담당 고객만 조회
     * - myCustomersOnly = false/null: 모든 활성 구독 고객 조회
     * - status = ACTIVE만 필터링
     * - 정렬: membershipLevel DESC, createdAt DESC
     * - 총 인원 수 함께 반환
     *
     * @param trainerId 트레이너 ID
     * @param myCustomersOnly 내 담당 고객만 조회 여부
     * @param pageable 페이징 정보
     * @return 구독 고객 슬라이스 (총 인원 수 포함)
     */
    @Override
    public SubscriptionCustomerSliceResponseDTO getActiveSubscriptionCustomers(
        Long trainerId,
        Boolean myCustomersOnly,
        Pageable pageable
    ) {
        // myCustomersOnly가 true이면 trainerId 필터 적용, 그 외에는 null (전체 조회)
        Long filterTrainerId = Boolean.TRUE.equals(myCustomersOnly) ? trainerId : null;

        // 1. 활성 구독 고객 목록 조회
        Slice<SubscriptionCustomerResponseDTO> slice =
            subscriptionRepository.findActiveSubscriptionCustomers(filterTrainerId, pageable);

        // 2. 활성 구독 고객 총 인원 수 조회
        Long totalCount = subscriptionRepository.countActiveSubscriptionCustomers(filterTrainerId);

        // 3. DTO 생성 및 반환
        return SubscriptionCustomerSliceResponseDTO.from(slice, totalCount);
    }
}
