package com.tradingpt.tpt_api.domain.investmenttypehistory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeChangeRequest;
import com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvestmentTypeChangeRequestRepository
	extends JpaRepository<InvestmentTypeChangeRequest, Long>, InvestmentTypeChangeRequestRepositoryCustom {

	/**
	 * 고객의 모든 변경 신청 조회 (최신순)
	 */
	List<InvestmentTypeChangeRequest> findByCustomerIdOrderByRequestedDateDesc(Long customerId);

	/**
	 * 특정 상태의 신청 조회 (오래된 순)
	 */
	List<InvestmentTypeChangeRequest> findByStatusOrderByRequestedDateAsc(ChangeRequestStatus status);


	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
        DELETE FROM InvestmentTypeChangeRequest r
        WHERE r.customer.id = :customerId
        """)
	void deleteByCustomerId(@Param("customerId") Long customerId);
}
