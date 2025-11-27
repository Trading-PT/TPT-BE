package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 트레이너 작성 매매일지 리스트 아이템 DTO
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "트레이너 작성 매매일지 목록 아이템")
public class TrainerWrittenFeedbackItemDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "투자 타입", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "제목")
	private String title;

	@Schema(description = "매매 복기")
	private String tradingReview;

	@Schema(description = "트레이너 이름")
	private String trainerName;

	@Schema(description = "전체 자산 대비 P&L")
	private BigDecimal totalAssetPnl;

	@Schema(description = "첨부 이미지 URL 목록")
	private List<String> imageUrls;

	@Schema(description = "작성 시간")
	private LocalDateTime createdAt;

	/**
	 * FeedbackRequest 엔티티에서 DTO 생성
	 */
	public static TrainerWrittenFeedbackItemDTO from(FeedbackRequest request) {
		return TrainerWrittenFeedbackItemDTO.builder()
			.id(request.getId())
			.investmentType(request.getInvestmentType())
			.title(request.getTitle())
			.tradingReview(request.getTradingReview())
			.trainerName(request.getCustomer().getUsername())
			.totalAssetPnl(request.getTotalAssetPnl())
			.imageUrls(
				request.getFeedbackRequestAttachments().stream()
					.map(FeedbackRequestAttachment::getFileUrl)
					.toList()
			)
			.createdAt(request.getCreatedAt())
			.build();
	}
}
