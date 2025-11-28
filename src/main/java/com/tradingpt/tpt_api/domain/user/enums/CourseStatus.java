package com.tradingpt.tpt_api.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CourseStatus {

	BEFORE_COMPLETION("완강 전"),
	PENDING_COMPLETION("완강 했지만 다음 달이 되지 않음"),
	AFTER_COMPLETION("완강 후"),
	;

	private final String description;

	/**
	 * 완강 전 그룹 여부 확인
	 * PENDING_COMPLETION과 BEFORE_COMPLETION은 동일하게 "완강 전" 그룹으로 취급
	 * (월 중간에 완강해도 다음 달까지는 BEFORE_COMPLETION으로 간주)
	 *
	 * @return 완강 전 그룹이면 true
	 */
	public boolean isBeforeCompletionGroup() {
		return this == BEFORE_COMPLETION || this == PENDING_COMPLETION;
	}

	/**
	 * 두 CourseStatus가 호환되는지 확인
	 * - 둘 다 "완강 전" 그룹이거나
	 * - 둘 다 AFTER_COMPLETION인 경우 호환
	 *
	 * @param other 비교할 CourseStatus
	 * @return 호환되면 true
	 */
	public boolean isCompatibleWith(CourseStatus other) {
		if (other == null) {
			return false;
		}
		// 둘 다 "완강 전" 그룹이거나 둘 다 AFTER_COMPLETION인 경우 호환
		return (this.isBeforeCompletionGroup() && other.isBeforeCompletionGroup()) ||
			(this == AFTER_COMPLETION && other == AFTER_COMPLETION);
	}

}
