package com.tradingpt.tpt_api.domain.review.dto.response;

import java.util.List;

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
@Schema(description = "리뷰 통계 응답 DTO")
public class ReviewStatisticsResponseDTO {

	@Schema(description = "전체 리뷰 개수", example = "1250")
	private Long totalReviewCount;

	@Schema(description = "평균 별점 (총 별점 합 / 총 리뷰 개수)", example = "4.7")
	private Double averageRating;

	@Schema(description = "태그별 리뷰 통계")
	private List<ReviewTagStatisticsResponseDTO> tagStatistics;

	/**
	 * 정적 팩토리 메서드
	 */
	public static ReviewStatisticsResponseDTO of(
		Long totalReviewCount,
		Long totalRatingSum,
		List<ReviewTagStatisticsResponseDTO> tagStatistics
	) {
		Double averageRating = (totalReviewCount != null && totalReviewCount > 0 && totalRatingSum != null)
			? Math.round((double) totalRatingSum / totalReviewCount * 10.0) / 10.0
			: 0.0;

		return ReviewStatisticsResponseDTO.builder()
			.totalReviewCount(totalReviewCount != null ? totalReviewCount : 0L)
			.averageRating(averageRating)
			.tagStatistics(tagStatistics)
			.build();
	}
}
