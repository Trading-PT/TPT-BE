package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;

import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 피드백 요청 날짜 관련 유틸리티.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedbackPeriodUtil {

	private static final WeekFields WEEK_FIELDS = WeekFields.of(DayOfWeek.MONDAY, 4);

	/**
	 * 요청 날짜를 기반으로 연/월/주차를 계산한다.
	 *
	 * @param date 기준이 되는 날짜 (null 불가)
	 * @return year, month, week 묶음
	 * @throws IllegalArgumentException date가 null일 때
	 */
	public static FeedbackPeriod resolveFrom(LocalDate date) {
		if (date == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.REQUEST_DATE_REQUIRED);
		}

		int year = date.getYear();
		int month = date.getMonthValue();
		int week = date.get(WEEK_FIELDS.weekOfMonth());
		if (week < 1) {
			week = 1;
		}

		return new FeedbackPeriod(year, month, week);
	}

	public record FeedbackPeriod(int year, int month, int week) {
	}
}
