package com.tradingpt.tpt_api.domain.investmenttypehistory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;

public interface InvestmentTypeHistoryRepository
	extends JpaRepository<InvestmentTypeHistory, Long>, InvestmentTypeHistoryRepositoryCustom {

	/**
	 * 특정 고객의 투자유형 이력 조회 (startDate 오름차순)
	 *
	 * @param customerId 고객 ID
	 * @return 투자유형 이력 리스트
	 */
	List<InvestmentTypeHistory> findByCustomer_IdOrderByStartDateAsc(Long customerId);
}
