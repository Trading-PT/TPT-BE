package com.tradingpt.tpt_api.global.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 토큰 보상 관련 상수 정의
 *
 * 매매일지 n개 작성 시 토큰 m개 발급 기능의 상수 관리
 * 이 클래스의 값을 변경하면 전체 시스템에 즉시 적용됨
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RewardConstants {

	/**
	 * 피드백 작성 임계값 (N개)
	 * 이 개수마다 토큰을 발급
	 *
	 * 예: 5로 설정 시, 5개, 10개, 15개... 작성 시마다 보상
	 */
	public static final int FEEDBACK_THRESHOLD = 5;

	/**
	 * 토큰 보상 개수 (M개)
	 * 임계값 도달 시 발급할 토큰 개수
	 *
	 * 예: 3으로 설정 시, 조건 만족 시마다 3토큰 발급
	 */
	public static final int TOKEN_REWARD_AMOUNT = 3;

	/**
	 * BASIC 멤버십 피드백 요청 시 기본 토큰 소모량
	 * 미구독자(BASIC 멤버십)가 토큰을 사용해 피드백을 요청할 때 소모되는 기본 토큰 개수
	 *
	 * 예: 3으로 설정 시, 피드백 요청 당 기본 3토큰 소모
	 */
	public static final int DEFAULT_TOKEN_CONSUMPTION = 3;
	
	/**
	 * 다음 보상까지 남은 개수 계산
	 *
	 * @param currentCount 현재 피드백 작성 개수
	 * @return 다음 보상까지 남은 피드백 개수
	 */
	public static int getRemainingForNextReward(int currentCount) {
		int remainder = currentCount % FEEDBACK_THRESHOLD;
		return FEEDBACK_THRESHOLD - remainder;
	}

	/**
	 * 현재 카운트가 보상 조건을 만족하는지 확인
	 *
	 * @param currentCount 현재 피드백 작성 개수
	 * @return 보상 조건 만족 여부
	 */
	public static boolean isRewardEligible(int currentCount) {
		return currentCount > 0 && currentCount % FEEDBACK_THRESHOLD == 0;
	}
}
