package com.tradingpt.tpt_api.domain.user.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.user.dto.response.FreeCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListResponseDTO;

public interface CustomerQueryService {

	/**
	 * 내 담당 고객 목록 조회 (무한 스크롤)
	 *
	 * @param trainerId 트레이너 ID
	 * @param pageable 페이징 정보
	 * @return 담당 고객 목록
	 */
	MyCustomerListResponseDTO getMyCustomers(Long trainerId, Pageable pageable);

	/**
	 * 미구독(무료) 고객 목록 조회
	 *
	 * 조건:
	 * - ACTIVE 상태의 Subscription이 없음
	 * - membershipLevel이 BASIC
	 * - 담당 트레이너가 없음
	 *
	 * @param pageable 페이징 정보
	 * @return 미구독 고객 Slice
	 */
	Slice<FreeCustomerResponseDTO> getFreeCustomers(Pageable pageable);
}
