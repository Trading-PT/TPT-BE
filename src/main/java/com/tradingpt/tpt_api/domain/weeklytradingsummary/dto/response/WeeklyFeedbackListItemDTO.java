package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 주간 피드백 목록 아이템 DTO (손익 필터링용)
 */
@Getter
@Builder
@Schema(description = "주간 피드백 정보 (손익 필터링)")
public class WeeklyFeedbackListItemDTO {

	@Schema(description = "피드백 요청 ID")
	private Long feedbackId;

	@Schema(description = "피드백 제목")
	private String title;

	@Schema(description = "피드백 요청 날짜", example = "2025-11-15")
	private LocalDate feedbackRequestDate;

	@Schema(description = "손익 (P&L)", example = "150.50")
	private BigDecimal totalAssetPnl;

	@Schema(description = "투자 타입", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "피드백 상태", example = "FR")
	private Status status;

	@Schema(description = "피드백 응답 여부")
	private Boolean hasResponse;

	public static WeeklyFeedbackListItemDTO from(FeedbackRequest feedbackRequest) {
		return WeeklyFeedbackListItemDTO.builder()
			.feedbackId(feedbackRequest.getId())
			.title(feedbackRequest.getTitle())
			.feedbackRequestDate(feedbackRequest.getFeedbackRequestDate())
			.totalAssetPnl(feedbackRequest.getTotalAssetPnl())
			.investmentType(feedbackRequest.getInvestmentType())
			.status(feedbackRequest.getStatus())
			.hasResponse(feedbackRequest.getFeedbackResponse() != null)
			.build();
	}
}
