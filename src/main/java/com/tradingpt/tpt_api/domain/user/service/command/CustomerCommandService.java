package com.tradingpt.tpt_api.domain.user.service.command;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

/**
 * Customer Command Service 인터페이스
 * Customer 엔티티에 대한 CUD 작업 정의
 */
public interface CustomerCommandService {

	/**
	 * 구독 기반 멤버십 레벨 업데이트
	 * 정기 결제 성공 시 호출되어 고객의 멤버십을 PREMIUM으로 승급하고 만료일 설정
	 *
	 * @param customerId 고객 ID
	 * @param membershipLevel 멤버십 레벨 (BASIC, PREMIUM)
	 * @param expiredAt 멤버십 만료 일시 (null이면 만료일 없음)
	 */
	void updateMembershipFromSubscription(
		Long customerId,
		MembershipLevel membershipLevel,
		LocalDateTime expiredAt
	);

	/**
	 * 특정 고객의 피드백 카운트를 실제 값으로 동기화
	 * 스케줄러에서 데이터 정합성 보장을 위해 사용
	 *
	 * @param customerId 동기화할 고객 ID
	 * @return 동기화된 카운트 (실제 피드백 요청 개수)
	 */
	int syncFeedbackCount(Long customerId);

	/**
	 * 모든 고객의 피드백 카운트를 실제 값으로 동기화
	 * 스케줄러에서 전체 데이터 정합성 보장을 위해 사용
	 *
	 * @return 동기화된 고객 수
	 */
	int syncAllFeedbackCounts();
}
