package com.tradingpt.tpt_api.domain.paymentmethod.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.BillingRequest;

public interface BillingRequestRepository extends JpaRepository<BillingRequest, Long> {

	Optional<BillingRequest> findByCustomer_IdAndMoid(Long customerId, String moid);

}
