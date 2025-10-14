package com.tradingpt.tpt_api.domain.investmenttypehistory.repository;

public interface InvestmentTypeChangeRequestRepositoryCustom {

	/**
	 * 고객의 대기 중인 변경 신청이 있는지 확인
	 *
	 * @param customerId 고객 ID
	 * @return 대기 중인 신청이 있으면 true, 없으면 false
	 */
	boolean existsPendingRequestByCustomerId(Long customerId);

}
