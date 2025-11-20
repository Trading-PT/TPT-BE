package com.tradingpt.tpt_api.domain.subscription.dto.response;

import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestGrade;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 고객별 집계 데이터 DTO
 * Batch fetching용 중간 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAggregationDTO {

	private Long customerId;

	// 레벨 테스트 관련
	private LevelTestGrade latestLevelTestGrade;
	private Integer latestLevelTestScore;

	// 강의 진도 관련
	private Long totalLectureProgressCount;
	private Long completedLectureCount;

	// 과제 관련
	private Long totalAssignmentCount;
	private Long unsubmittedAssignmentCount;

	/**
	 * 강의 완료율 계산 (%)
	 * @return 0-100 사이의 완료율, 전체 강의가 0이면 0.0 반환
	 */
	public Double calculateLectureCompletionRate() {
		if (totalLectureProgressCount == null || totalLectureProgressCount == 0) {
			return 0.0;
		}
		if (completedLectureCount == null) {
			return 0.0;
		}
		return (completedLectureCount.doubleValue() / totalLectureProgressCount.doubleValue()) * 100.0;
	}

	/**
	 * 과제 제출율 계산 (%)
	 * @return 0-100 사이의 제출율, 전체 과제가 0이면 100.0 반환
	 */
	public Double calculateAssignmentSubmissionRate() {
		if (totalAssignmentCount == null || totalAssignmentCount == 0) {
			return 100.0;  // 과제가 없으면 100%
		}
		long submittedCount = totalAssignmentCount - (unsubmittedAssignmentCount != null ? unsubmittedAssignmentCount : 0);
		return (submittedCount / totalAssignmentCount.doubleValue()) * 100.0;
	}

	/**
	 * 레벨테스트 응시 여부
	 */
	public boolean hasLevelTestResult() {
		return latestLevelTestGrade != null && latestLevelTestScore != null;
	}
}
