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

	@Schema(description = "고객 전화번호")
	private String phoneNumber;

	@Schema(description = "리뷰 내용")
	private String content;

	@Schema(description = "리뷰 별점 (1-5)", example = "5")
	private Integer rating;

	@Schema(description = "리뷰 작성 일시")
	private LocalDateTime submittedAt;

	@Schema(description = "트레이너 답변 (답변이 있는 경우)")
	private TrainerReplyResponseDTO trainerReply;

	@Schema(description = "고객 공개 허용 여부")
	private Boolean isPublic;

	/**
	 * Review 엔티티를 DTO로 변환
	 */
	public static ReviewResponseDTO from(Review review) {
		ReviewResponseDTOBuilder builder = ReviewResponseDTO.builder()
			.id(review.getId())
			.customerId(review.getCustomer().getId())
			.customerName(review.getCustomer().getName())
			.phoneNumber(review.getCustomer().getPhoneNumber())
			.content(review.getContent())
			.rating(review.getRating())
			.submittedAt(review.getSubmittedAt())
			.isPublic(review.isPublic());

		// 답변이 있는 경우 TrainerReply 추가
		if (review.hasReply()) {
			builder.trainerReply(TrainerReplyResponseDTO.builder()
				.trainerId(review.getTrainer().getId())
				.replyContent(review.getReplyContent())
				.repliedAt(review.getRepliedAt())
				.build());
		}

		return builder.build();
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
	}

}
