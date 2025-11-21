package com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 개별 매매의 R&R 계산용 데이터 (Repository 조회 전용)
 * 수익 매매(pnl > 0)의 평균 R&R 계산에 사용
 */
@Getter
@AllArgsConstructor
public class TradeRnRData {
	private BigDecimal pnl;          // 개별 매매 P&L
	private BigDecimal riskTaking;   // 개별 매매 리스크 테이킹
}
