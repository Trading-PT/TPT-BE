package com.tradingpt.tpt_api.domain.user.user.repository;

import com.tradingpt.tpt_api.domain.user.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
