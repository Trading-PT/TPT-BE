package com.tradingpt.tpt_api.user.repository;

import com.tradingpt.tpt_api.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
