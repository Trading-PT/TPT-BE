package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.TradeRnRData;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 트레이딩 통계 계산 유틸리티 클래스
 *
 * <p>매매 성과 분석에 필요한 주요 지표를 계산합니다.</p>
 *
 * <h3>제공하는 계산:</h3>
 * <ul>
 *   <li>승률(Win Rate): (승리 횟수 / 전체 매매 횟수) × 100</li>
 *   <li>평균 R&R(Risk-Reward Ratio): P&L / 리스크 테이킹</li>
 * </ul>
 *
 * @author TradingPT
 * @since 1.0
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TradingCalculationUtil {

	/**
	 * 승률을 계산합니다.
	 *
	 * <p><b>공식:</b> (승리 횟수 / 전체 매매 횟수) × 100</p>
	 * <p><b>결과:</b> 소수점 둘째 자리까지 반올림 (예: 65.43%)</p>
	 *
	 * @param totalCount 전체 매매 횟수
	 * @param winCount 승리 횟수 (P&L > 0인 매매)
	 * @return 승률 (0.00 ~ 100.00), 매매 횟수가 0이면 0.0 반환
	 *
	 * @throws FeedbackRequestException 잘못된 입력값인 경우
	 *
	 * @example
	 * <pre>{@code
	 * // 10번 매매 중 7번 승리
	 * Double winRate = TradingCalculationUtil.calculateWinRate(10, 7);
	 * // 결과: 70.00
	 *
	 * // 매매 없음
	 * Double winRate = TradingCalculationUtil.calculateWinRate(0, 0);
	 * // 결과: 0.0
	 * }</pre>
	 */
	public static Double calculateWinRate(Integer totalCount, Integer winCount) {
		validateWinRateInputs(totalCount, winCount);

		if (totalCount == 0) {
			return 0.0;
		}

		return BigDecimal.valueOf(winCount)
			.divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
			.multiply(BigDecimal.valueOf(100))
			.setScale(2, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * 평균 R&R(Risk-Reward Ratio)을 계산합니다.
	 *
	 * <p><b>공식:</b> P&L / 리스크 테이킹</p>
	 * <p><b>결과:</b> 소수점 둘째 자리까지 반올림 (예: 1.50)</p>
	 *
	 * @param totalPnl 총 손익 (Profit and Loss)
	 * @param totalRiskTaking 총 리스크 테이킹 (진입 시 위험 금액의 합)
	 * @return 평균 R&R, 리스크 테이킹이 0이면 0.0 반환
	 *
	 * @throws FeedbackRequestException 잘못된 입력값인 경우
	 *
	 * @example
	 * <pre>{@code
	 * // P&L 150만원, 리스크 100만원
	 * Double rnr = TradingCalculationUtil.calculateAverageRnR(
	 *     new BigDecimal("1500000"),
	 *     new BigDecimal("1000000")
	 * );
	 * // 결과: 1.50
	 *
	 * // 손실 (P&L -50만원, 리스크 100만원)
	 * Double rnr = TradingCalculationUtil.calculateAverageRnR(
	 *     new BigDecimal("-500000"),
	 *     new BigDecimal("1000000")
	 * );
	 * // 결과: -0.50
	 * }</pre>
	 */
	public static Double calculateAverageRnR(BigDecimal totalPnl, BigDecimal totalRiskTaking) {
		validateRnRInputs(totalPnl, totalRiskTaking);

		if (totalRiskTaking.compareTo(BigDecimal.ZERO) == 0) {
			return 0.0;
		}

		return totalPnl
			.divide(totalRiskTaking, 2, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * 수익 매매들의 평균 R&R(Risk-Reward Ratio)을 계산합니다.
	 *
	 * <p><b>공식:</b> (각 수익 매매의 R&R 합계) / 수익 매매 횟수</p>
	 * <p><b>수익 매매:</b> pnl > 0인 매매만 포함</p>
	 * <p><b>개별 R&R:</b> pnl / riskTaking</p>
	 * <p><b>결과:</b> 소수점 둘째 자리까지 반올림 (예: 2.34)</p>
	 *
	 * @param trades 개별 매매 데이터 리스트 (pnl, riskTaking 포함)
	 * @return 수익 매매들의 평균 R&R, 수익 매매가 없으면 0.0 반환
	 *
	 * @example
	 * <pre>{@code
	 * // 수익 매매 3건
	 * // 1번: pnl=+100, riskTaking=50 -> rnr=2.0
	 * // 2번: pnl=+150, riskTaking=50 -> rnr=3.0
	 * // 3번: pnl=+80, riskTaking=40 -> rnr=2.0
	 * // 평균 = (2.0 + 3.0 + 2.0) / 3 = 2.33
	 *
	 * List<TradeRnRData> trades = Arrays.asList(
	 *     new TradeRnRData(new BigDecimal("100"), new BigDecimal("50")),
	 *     new TradeRnRData(new BigDecimal("150"), new BigDecimal("50")),
	 *     new TradeRnRData(new BigDecimal("80"), new BigDecimal("40"))
	 * );
	 * Double avgRnR = TradingCalculationUtil.calculateAverageRnRFromWinningTrades(trades);
	 * // 결과: 2.33
	 *
	 * // 수익 매매가 없는 경우
	 * List<TradeRnRData> noWinningTrades = Collections.emptyList();
	 * Double avgRnR = TradingCalculationUtil.calculateAverageRnRFromWinningTrades(noWinningTrades);
	 * // 결과: 0.0
	 * }</pre>
	 */
	public static Double calculateAverageRnRFromWinningTrades(List<TradeRnRData> trades) {
		if (trades == null || trades.isEmpty()) {
			return 0.0;
		}

		// 각 수익 매매의 R&R 계산 (rnr = pnl / riskTaking)
		BigDecimal totalRnR = BigDecimal.ZERO;
		int count = 0;

		for (TradeRnRData trade : trades) {
			BigDecimal pnl = trade.getPnl();
			BigDecimal riskTaking = trade.getRiskTaking();

			// pnl > 0 체크 (추가 안전장치, 쿼리에서도 필터링됨)
			if (pnl.compareTo(BigDecimal.ZERO) > 0 && riskTaking.compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal individualRnR = pnl.divide(riskTaking, 2, RoundingMode.HALF_UP);
				totalRnR = totalRnR.add(individualRnR);
				count++;
			}
		}

		// 수익 매매가 없으면 0.0 반환
		if (count == 0) {
			return 0.0;
		}

		// 평균 계산
		return totalRnR
			.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
			.doubleValue();
	}

	/**
	 * 승률 계산 입력값의 유효성을 검증합니다.
	 *
	 * @param totalCount 전체 매매 횟수
	 * @param winCount 승리 횟수
	 * @throws FeedbackRequestException 유효하지 않은 입력인 경우
	 */
	private static void validateWinRateInputs(Integer totalCount, Integer winCount) {
		if (totalCount == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_WIN_RATE_TOTAL_COUNT_NULL);
		}
		if (winCount == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_WIN_RATE_WIN_COUNT_NULL);
		}
		if (totalCount < 0) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_WIN_RATE_TOTAL_COUNT_NEGATIVE);
		}
		if (winCount < 0) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_WIN_RATE_WIN_COUNT_NEGATIVE);
		}
		if (winCount > totalCount) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_WIN_RATE_WIN_GREATER_THAN_TOTAL);
		}
	}

	/**
	 * R&R 계산 입력값의 유효성을 검증합니다.
	 *
	 * @param totalPnl 총 손익
	 * @param totalRiskTaking 총 리스크 테이킹
	 * @throws FeedbackRequestException 유효하지 않은 입력인 경우
	 */
	private static void validateRnRInputs(BigDecimal totalPnl, BigDecimal totalRiskTaking) {
		if (totalPnl == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_RNR_PNL_NULL);
		}
		if (totalRiskTaking == null) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_RNR_RISK_TAKING_NULL);
		}
		if (totalRiskTaking.compareTo(BigDecimal.ZERO) < 0) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.INVALID_RNR_RISK_TAKING_NEGATIVE);
		}
	}
}