package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.user.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {
	Page<Customer> findByAssignedTrainer_Id(Long trainerId, Pageable pageable);

	// 트레이너에 배정된 고객 리스트 조회
	List<Customer> findByAssignedTrainer_Id(Long trainerId);

	boolean existsByAssignedTrainer_Id(Long trainerId);

	Slice<Customer> findByAssignedTrainerIdOrderByMembershipLevelDescCreatedAtDesc(Long trainerId, Pageable pageable);
}
