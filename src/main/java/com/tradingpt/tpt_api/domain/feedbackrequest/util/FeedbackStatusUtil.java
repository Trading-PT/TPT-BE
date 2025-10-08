package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 피드백 요청 상태 판단 유틸리티 클래스
 *
 * <p>여러 피드백 요청의 상태를 기반으로 대표 상태를 결정합니다.</p>
 *
 * <h3>우선순위 규칙:</h3>
 * <ul>
 *   <li>FN (안 읽음)이 하나라도 있으면 → FN</li>
 *   <li>모두 FR (읽음)이면 → FR</li>
 * </ul>
 *
 * @author TradingPT
 * @since 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FeedbackStatusUtil {

	/**
	 * 피드백 요청들의 읽음 상태를 기반으로 대표 상태를 결정합니다.
	 *
	 * <p><b>우선순위:</b> FN (안 읽음) > FR (읽음)</p>
	 *
	 * @param fnCount Status.FN인 피드백 요청 개수
	 * @return 결정된 대표 Status (FN 또는 FR)
	 *
	 * @throws IllegalArgumentException fnCount가 음수인 경우
	 *
	 * @example
	 * <pre>{@code
	 * // 안 읽은 피드백이 2개 있는 경우
	 * Status status = FeedbackStatusUtil.determineReadStatus(2);
	 * // 결과: Status.FN
	 *
	 * // 모두 읽은 경우
	 * Status status = FeedbackStatusUtil.determineReadStatus(0);
	 * // 결과: Status.FR
	 * }</pre>
	 */
	public static Status determineReadStatus(Integer fnCount) {
		validateCount(fnCount);

		return fnCount > 0 ? Status.FN : Status.N;
	}

	/**
	 * 피드백 요청들의 전체 상태를 기반으로 대표 상태를 결정합니다.
	 *
	 * <p><b>우선순위:</b> N (답변 대기) > FN (안 읽음) > FR (읽음)</p>
	 *
	 * <p><b>Note:</b> 현재는 N 상태를 반환하지 않지만, 향후 확장을 위해 메서드를 분리했습니다.</p>
	 *
	 * @param nCount Status.N인 피드백 요청 개수
	 * @param fnCount Status.FN인 피드백 요청 개수
	 * @return 결정된 대표 Status (현재는 FN 또는 FR만 반환)
	 *
	 * @throws FeedbackRequestException nCount 또는 fnCount가 음수인 경우
	 */
	public static Status determineStatus(Integer nCount, Integer fnCount) {
		validateCount(nCount);
		validateCount(fnCount);

		// 현재는 N 상태를 반환하지 않음
		// 향후 요구사항 변경 시 아래 주석 해제
		// if (nCount > 0) {
		//     return Status.N;
		// }

		return fnCount > 0 ? Status.FN : Status.N;
	}

	/**
	 * 개수 값의 유효성을 검증합니다.
	 *
	 * @param count 검증할 개수
	 * @throws FeedbackRequestException count가 null이거나 음수인 경우
	 */
	private static void validateCount(Integer count) {
		if (count == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_READ_STATUS_CANNOT_BE_NULL);
		}
		if (count < 0) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_READ_STATUS_CANNOT_BE_MINUS);
		}
	}
}