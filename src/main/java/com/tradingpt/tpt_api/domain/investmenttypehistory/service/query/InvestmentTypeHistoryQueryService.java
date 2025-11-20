package com.tradingpt.tpt_api.domain.investmenttypehistory.service.query;

import java.util.List;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.InvestmentTypeHistoryResponseDTO;

/**
 * 투자유형 이력 조회 서비스
 */
public interface InvestmentTypeHistoryQueryService {

	/**
	 * 특정 고객의 투자유형 이력 조회 (startDate 오름차순)
	 *
	 * @param customerId 고객 ID
	 * @return 투자유형 이력 리스트
	 */
	List<InvestmentTypeHistoryResponseDTO> getCustomerInvestmentTypeHistories(Long customerId);
}
