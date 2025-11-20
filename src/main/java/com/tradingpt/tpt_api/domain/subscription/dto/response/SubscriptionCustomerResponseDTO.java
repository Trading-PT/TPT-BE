package com.tradingpt.tpt_api.domain.subscription.dto.response;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestGrade;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 구독 고객 목록 응답 DTO
 * 관리자용 구독 고객 목록 조회 시 사용
 */
@Getter
@Schema(description = "구독 고객 정보 응답")
public class SubscriptionCustomerResponseDTO {

	// ========== 기본 구독 정보 ==========
	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객명")
	private String name;

	@Schema(description = "전화번호")
	private String phoneNumber;

	@Schema(description = "투자 유형")
	private InvestmentType investmentType;

	@Schema(description = "멤버십 레벨")
	private MembershipLevel membershipLevel;

	@Schema(description = "구독 상태")
	private Status subscriptionStatus;

	@Schema(description = "구독 플랜명")
	private String subscriptionPlanName;

	@Schema(description = "다음 결제 예정일")
	private LocalDate nextBillingDate;

	@Schema(description = "현재 결제 주기 종료일")
	private LocalDate currentPeriodEnd;

	@Schema(description = "배정된 트레이너 이름 (미배정 시 null)")
	private String trainerName;

	// ========== 집계 데이터 (Batch Fetching 후 설정) ==========
	@Setter
	@Schema(description = "최근 레벨테스트 등급 (미응시 시 null)")
	private LevelTestGrade lastLevelTestGrade;

	@Setter
	@Schema(description = "최근 레벨테스트 점수 (미응시 시 null)")
	private Integer lastLevelTestScore;

	@Setter
	@Schema(description = "완료한 강의 수")
	private Long completedLectureCount;

	@Setter
	@Schema(description = "전체 강의 진행 수 (고객별)")
	private Long totalLectureProgressCount;

	@Setter
	@Schema(description = "미제출 과제 수")
	private Long unsubmittedAssignmentCount;

	@Setter
	@Schema(description = "전체 할당된 과제 수")
	private Long totalAssignmentCount;

	/**
	 * Projections.constructor() 생성자
	 * 기본 구독 정보만 설정 (집계 데이터는 나중에 batch fetching으로 설정)
	 */
	public SubscriptionCustomerResponseDTO(
		Long customerId,
		String name,
		String phoneNumber,
		InvestmentType investmentType,
		MembershipLevel membershipLevel,
		Status subscriptionStatus,
		String subscriptionPlanName,
		LocalDate nextBillingDate,
		LocalDate currentPeriodEnd,
		String trainerName
	) {
		this.customerId = customerId;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.investmentType = investmentType;
		this.membershipLevel = membershipLevel;
		this.subscriptionStatus = subscriptionStatus;
		this.subscriptionPlanName = subscriptionPlanName;
		this.nextBillingDate = nextBillingDate;
		this.currentPeriodEnd = currentPeriodEnd;
		this.trainerName = trainerName;
	}

	// ========== 계산된 속성 (Getter Methods) ==========

	/**
	 * 강의 완료율 (%) 계산
	 * @return 0-100 사이의 완료율, 전체 강의가 0이면 0.0 반환
	 */
	@Schema(description = "강의 완료율 (%)", example = "65.5")
	public Double getLectureCompletionRate() {
		if (totalLectureProgressCount == null || totalLectureProgressCount == 0) {
			return 0.0;
		}
		if (completedLectureCount == null) {
			return 0.0;
		}
		return (completedLectureCount.doubleValue() / totalLectureProgressCount.doubleValue()) * 100.0;
	}

	/**
	 * 과제 제출율 (%) 계산
	 * @return 0-100 사이의 제출율, 전체 과제가 0이면 100.0 반환
	 */
	@Schema(description = "과제 제출율 (%)", example = "80.0")
	public Double getAssignmentSubmissionRate() {
		if (totalAssignmentCount == null || totalAssignmentCount == 0) {
			return 100.0;  // 과제가 없으면 100%
		}
		long submittedCount = totalAssignmentCount - (unsubmittedAssignmentCount != null ? unsubmittedAssignmentCount : 0);
		return (submittedCount / totalAssignmentCount.doubleValue()) * 100.0;
	}

	/**
	 * 레벨테스트 응시 여부
	 */
	@Schema(description = "레벨테스트 응시 여부")
	public boolean hasLevelTestResult() {
		return lastLevelTestGrade != null && lastLevelTestScore != null;
	}

	/**
	 * 집계 데이터를 한 번에 설정하는 헬퍼 메서드
	 */
	public void enrichWithAggregations(CustomerAggregationDTO aggregation) {
		if (aggregation != null) {
			this.lastLevelTestGrade = aggregation.getLatestLevelTestGrade();
			this.lastLevelTestScore = aggregation.getLatestLevelTestScore();
			this.completedLectureCount = aggregation.getCompletedLectureCount();
			this.totalLectureProgressCount = aggregation.getTotalLectureProgressCount();
			this.unsubmittedAssignmentCount = aggregation.getUnsubmittedAssignmentCount();
			this.totalAssignmentCount = aggregation.getTotalAssignmentCount();
		}
	}
}
