package com.tradingpt.tpt_api.domain.user.service.query;

import org.springframework.data.domain.Pageable;

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
}
