package com.tradingpt.tpt_api.domain.user.repository;

import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {
	Page<Customer> findByAssignedTrainer_Id(Long trainerId, Pageable pageable);

	// 트레이너에 배정된 고객 리스트 조회
	List<Customer> findByAssignedTrainer_Id(Long trainerId);

	boolean existsByAssignedTrainer_Id(Long trainerId);

	Slice<Customer> findByAssignedTrainerIdOrderByMembershipLevelDescCreatedAtDesc(Long trainerId, Pageable pageable);

	/**
	 * 특정 멤버십 레벨이고 만료일이 지난 고객 조회
	 * 만료 처리 스케줄러에서 사용
	 *
	 * @param membershipLevel 멤버십 레벨
	 * @param expiredAt 만료 기준 일시
	 * @return 만료된 고객 목록
	 */
	List<Customer> findByMembershipLevelAndMembershipExpiredAtBefore(
		MembershipLevel membershipLevel,
		LocalDateTime expiredAt
	);

	// 여러 UID 상태 조회
	List<Customer> findByUserStatusIn(List<UserStatus> statuses);
}
