package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.user.enums.CourseStatus.PENDING_COMPLETION;

import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;
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
	 * 트레이너의 담당 고객 중 특정 완강 상태인 고객을 이름순으로 조회
	 * 평가 관리 화면에서 사용
	 *
	 * @param trainerId    트레이너 ID
	 * @param courseStatus 완강 상태
	 * @param pageable     페이징 정보
	 * @return 조건에 맞는 고객 목록 (Slice)
	 */
	Slice<Customer> findByAssignedTrainerIdAndCourseStatusOrderByNameAsc(
		Long trainerId,
		CourseStatus courseStatus,
		Pageable pageable
	);

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

	List<Customer> findAllByCourseStatus(CourseStatus PENDING_COMPLETION);

	Page<Customer> findByUidUidStartingWithIgnoreCase(String uidPrefix, Pageable pageable);

	Page<Customer> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
