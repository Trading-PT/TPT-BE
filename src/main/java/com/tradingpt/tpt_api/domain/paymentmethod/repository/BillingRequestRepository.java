package com.tradingpt.tpt_api.domain.paymentmethod.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.BillingRequest;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BillingRequestRepository extends JpaRepository<BillingRequest, Long> {

	Optional<BillingRequest> findByCustomer_IdAndMoid(Long customerId, String moid);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
        DELETE FROM BillingRequest b
        WHERE b.customer.id = :customerId
        """)
	void deleteByCustomerId(@Param("customerId") Long customerId);

}
