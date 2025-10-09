package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 날짜 검증 유틸리티
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateValidationUtil {

	/**
	 * 연도/월이 현재 또는 과거인지 검증합니다.
	 *
	 * @param year 검증할 연도
	 * @param month 검증할 월
	 * @throws FeedbackRequestException 미래 날짜인 경우
	 */
	public static void validatePastOrPresentYearMonth(Integer year, Integer month) {
		if (year == null || month == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_YEAR_MONTH);
		}

		if (month < 1 || month > 12) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_YEAR_MONTH);
		}

		LocalDate now = LocalDate.now();
		LocalDate target = LocalDate.of(year, month, 1);
		LocalDate currentMonth = LocalDate.of(now.getYear(), now.getMonthValue(), 1);

		if (target.isAfter(currentMonth)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_YEAR_MONTH);
		}
	}

	/**
	 * 연도가 현재 또는 과거인지 검증합니다.
	 *
	 * @param year 검증할 연도
	 * @throws FeedbackRequestException 미래 연도인 경우
	 */
	public static void validatePastOrPresentYear(Integer year) {
		if (year == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_YEAR_MONTH);
		}

		int currentYear = LocalDate.now().getYear();

		if (year > currentYear) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_YEAR_MONTH);
		}
	}
}