package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;

import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 날짜 검증 유틸리티
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateValidationUtil {

	/**
	 * 연도/월이 현재 또는 과거인지 검증합니다.
	 *
	 * @param year  검증할 연도
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

	/**
	 * 연도/월/일이 유효한 날짜인지 검증합니다.
	 *
	 * @param year  검증할 연도
	 * @param month 검증할 월
	 * @param day   검증할 일
	 * @throws FeedbackRequestException 유효하지 않은 날짜인 경우
	 */
	public static void validateDate(Integer year, Integer month, Integer day) {
		if (year == null || month == null || day == null) {
			log.warn("Date parameters cannot be null: year={}, month={}, day={}", year, month, day);
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_DATE);
		}

		// 월 범위 검증
		if (month < 1 || month > 12) {
			log.warn("Invalid month: {}", month);
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_DATE);
		}

		// 일 범위 기본 검증
		if (day < 1 || day > 31) {
			log.warn("Invalid day: {}", day);
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_DATE);
		}

		try {
			// 해당 연/월의 마지막 날 계산
			YearMonth yearMonth = YearMonth.of(year, month);
			int lastDayOfMonth = yearMonth.lengthOfMonth();

			// 해당 월의 유효한 날짜인지 확인
			if (day > lastDayOfMonth) {
				log.warn("Invalid day {} for year-month {}-{} (max: {})",
					day, year, month, lastDayOfMonth);
				throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_DATE);
			}

			// 실제 날짜 생성 가능한지 최종 검증
			LocalDate.of(year, month, day);
		} catch (DateTimeException e) {
			log.error("Invalid date: {}-{}-{}", year, month, day, e);
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_DATE);
		}
	}

	/**
	 * 연도/월/일이 현재 또는 과거인지 검증합니다.
	 *
	 * @param year  검증할 연도
	 * @param month 검증할 월
	 * @param day   검증할 일
	 * @throws FeedbackRequestException 미래 날짜이거나 유효하지 않은 날짜인 경우
	 */
	public static void validatePastOrPresentDate(Integer year, Integer month, Integer day) {
		// 먼저 날짜 유효성 검증
		validateDate(year, month, day);

		// 현재 날짜와 비교
		LocalDate now = LocalDate.now();
		LocalDate target = LocalDate.of(year, month, day);

		if (target.isAfter(now)) {
			log.warn("Future date not allowed: {}", target);
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_DATE);
		}
	}

	/**
	 * 유효한 날짜로 LocalDate 객체 생성
	 *
	 * @param year  연도
	 * @param month 월
	 * @param day   일
	 * @return LocalDate 객체
	 * @throws FeedbackRequestException 유효하지 않은 날짜인 경우
	 */
	public static LocalDate toLocalDate(Integer year, Integer month, Integer day) {
		validateDate(year, month, day);
		return LocalDate.of(year, month, day);
	}

	/**
	 * 연도/월/주가 유효한지 검증합니다.
	 *
	 * @param year  검증할 연도
	 * @param month 검증할 월
	 * @param week  검증할 주 (1-5)
	 * @throws FeedbackRequestException 유효하지 않은 날짜인 경우
	 */
	public static void validateYearMonthWeek(Integer year, Integer month, Integer week) {
		// 1. 연도/월 검증
		validatePastOrPresentYearMonth(year, month);

		// 2. 주 검증 (1-5)
		if (week == null || week < 1 || week > 5) {
			log.warn("Invalid week: {}", week);
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_YEAR_MONTH);
		}
	}
}