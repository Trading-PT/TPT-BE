package com.tradingpt.tpt_api.domain.review.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.review.entity.Review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponseDTO {

	@Schema(description = "리뷰 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름(아이디)")
	private String customerName;

	@Schema(description = "리뷰 내용")
	private String content;

	@Schema(description = "리뷰 작성 일시")
	private LocalDateTime submittedAt;

	@Schema(description = "트레이너 답변 (답변이 있는 경우)")
	private TrainerReplyResponseDTO trainerReply;

	public static ReviewResponseDTO from(Review review) {
		return ReviewResponseDTO.builder()
			.id(review.getId())
			.customerId(review.getCustomer().getId())
			.customerName(review.getCustomer().getName())
			.content(review.getContent())
			.submittedAt(review.getSubmittedAt())
			.build();
	}

	public boolean hasReply() {
		return trainerReply != null;
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Schema(description = "트레이너 답변 (답변이 있는 경우)")
	public static class TrainerReplyResponseDTO {

		@Schema(description = "트레이너 ID")
		private Long trainerId;

		@Schema(description = "답변 내용")
		private String replyContent;

		@Schema(description = "리뷰에 대한 트레이너의 답변 일시")
		private LocalDateTime repliedAt;

		public static ReviewResponseDTO from(Review review) {
			ReviewResponseDTOBuilder builder = ReviewResponseDTO.builder()
				.id(review.getId())
				.customerId(review.getCustomer().getId())
				.customerName(review.getCustomer().getName())
				.content(review.getContent())
				.submittedAt(review.getSubmittedAt());

			if (review.isAnswered() && review.getReplyContent() != null) {
				builder.trainerReply(TrainerReplyResponseDTO.builder()
					.trainerId(review.getTrainer().getId())
					.replyContent(review.getReplyContent())
					.repliedAt(review.getRepliedAt())
					.build());
			}

			return builder.build();
		}
	}

}
