package com.tradingpt.tpt_api.domain.investmenthistory.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.investmenthistory.entity.InvestmentHistory;

public interface InvestmentHistoryRepository extends JpaRepository<InvestmentHistory, Long> {

	@Query("""
		SELECT ih
		FROM InvestmentHistory ih
		WHERE ih.customer.id = :customerId
			AND ih.startedAt <= :targetDate
			AND (ih.endedAt IS NULL OR ih.endedAt >= :targetDate)
		ORDER BY ih.startedAt DESC
	""")
	Optional<InvestmentHistory> findActiveHistory(
		@Param("customerId") Long customerId,
		@Param("targetDate") LocalDate targetDate
	);
}
