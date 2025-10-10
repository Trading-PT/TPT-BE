package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Locale;

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

	/**
	 * 해당 월의 주차 수를 계산합니다.
	 *
	 * @param year 연도
	 * @param month 월
	 * @return 해당 월의 주차 수 (보통 4~5주)
	 */
	public static int getWeeksInMonth(Integer year, Integer month) {
		LocalDate firstDay = LocalDate.of(year, month, 1);
		LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());

		WeekFields weekFields = WeekFields.of(Locale.getDefault());

		int firstWeek = firstDay.get(weekFields.weekOfMonth());
		int lastWeek = lastDay.get(weekFields.weekOfMonth());

		return lastWeek - firstWeek + 1;
	}

	/**
	 * 해당 연도/월/주차의 날짜 범위를 반환합니다.
	 * 월요일을 주의 시작으로 계산합니다.
	 *
	 * @param year 연도
	 * @param month 월
	 * @param week 주차
	 * @return 해당 주차의 시작일과 종료일
	 */
	public static WeekDateRange getWeekDateRange(Integer year, Integer month, Integer week) {
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

		// 해당 월의 첫 번째 월요일 찾기
		LocalDate firstMonday;
		if (firstDayOfMonth.getDayOfWeek() == DayOfWeek.MONDAY) {
			firstMonday = firstDayOfMonth;
		} else if (firstDayOfMonth.getDayOfWeek().getValue() < DayOfWeek.MONDAY.getValue()) {
			// 월의 시작이 월요일보다 이전 (일요일은 불가능하지만 안전하게)
			firstMonday = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		} else {
			// 월의 시작이 화~일요일
			firstMonday = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
		}

		LocalDate startOfWeek;
		LocalDate endOfWeek;

		if (week == 1) {
			// 1주차: 월의 첫날부터 첫 번째 일요일까지
			startOfWeek = firstDayOfMonth;
			if (firstDayOfMonth.getDayOfWeek() == DayOfWeek.MONDAY) {
				// 1일이 월요일이면 1일~7일(일요일)
				endOfWeek = firstDayOfMonth.plusDays(6);
			} else {
				// 1일이 월요일이 아니면 그 주의 일요일까지
				endOfWeek = firstDayOfMonth.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
			}
		} else {
			// 2주차 이상: (week - 1)번째 월요일부터 일요일까지
			startOfWeek = firstMonday.plusWeeks(week - 2);
			endOfWeek = startOfWeek.plusDays(6);
		}

		// 월의 마지막날을 넘지 않도록 제한
		if (endOfWeek.isAfter(lastDayOfMonth)) {
			endOfWeek = lastDayOfMonth;
		}

		return new WeekDateRange(startOfWeek, endOfWeek);
	}

	public record FeedbackPeriod(int year, int month, int week) {
	}

	public record WeekDateRange(LocalDate startDate, LocalDate endDate) {
	}
}