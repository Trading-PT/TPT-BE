package com.tradingpt.tpt_api.domain.subscription.config;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 프로모션 설정 상수
 * 특정 기간 동안 가입한 고객에게 프로모션 혜택 제공
 */
public class PromotionConfig {

	/**
	 * 프로모션 시작일 (포함)
	 * 이 날짜 이후에 가입한 고객만 프로모션 대상
	 */
	public static final LocalDate PROMOTION_START_DATE = LocalDate.of(2025, 12, 5);

	/**
	 * 프로모션 종료일 (포함)
	 * 이 날짜 이전에 가입한 고객만 프로모션 대상
	 */
	public static final LocalDate PROMOTION_END_DATE = LocalDate.of(2025, 12, 17);

	/**
	 * 프로모션 무료 제공 기간 (개월)
	 * 프로모션 대상 고객은 이 기간 동안 무료 또는 최소 금액으로 구독 가능
	 */
	public static final int PROMOTION_FREE_MONTHS = 2;

	/**
	 * 프로모션 첫 결제 금액
	 * 0원 = 완전 무료
	 */
	public static final BigDecimal PROMOTION_PAYMENT_AMOUNT = BigDecimal.ZERO;

	/**
	 * 결제 실패 허용 횟수
	 * 이 횟수 이상 연속 실패 시 구독 상태가 PAYMENT_FAILED로 변경됨
	 */
	public static final int MAX_PAYMENT_FAILURE_COUNT = 3;

	/**
	 * 특정 날짜가 프로모션 기간 내인지 확인
	 *
	 * @param date 확인할 날짜
	 * @return 프로모션 기간 내이면 true
	 */
	public static boolean isWithinPromotionPeriod(LocalDate date) {
		return !date.isBefore(PROMOTION_START_DATE) && !date.isAfter(PROMOTION_END_DATE);
	}

	/**
	 * 프로모션 혜택 종료일 계산
	 * 구독 생성일로부터 N개월 후
	 *
	 * @param subscriptionCreatedDate 구독 생성일
	 * @return 프로모션 혜택 종료일
	 */
	public static LocalDate calculatePromotionEndDate(LocalDate subscriptionCreatedDate) {
		return subscriptionCreatedDate.plusMonths(PROMOTION_FREE_MONTHS);
	}
}
