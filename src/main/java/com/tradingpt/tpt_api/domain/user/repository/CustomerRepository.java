package com.tradingpt.tpt_api.domain.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.user.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
	Page<Customer> findByTrainer_Id(Long trainerId, Pageable pageable);
}
