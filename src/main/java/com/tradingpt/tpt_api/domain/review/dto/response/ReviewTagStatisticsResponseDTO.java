package com.tradingpt.tpt_api.domain.review.dto.response;

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
@Schema(description = "리뷰 태그별 통계 응답 DTO")
public class ReviewTagStatisticsResponseDTO {

	@Schema(description = "태그 ID", example = "1")
	private Long tagId;

	@Schema(description = "태그 이름", example = "맞춤 상담 300만원 어치의 가치가 있어요")
	private String tagName;

	@Schema(description = "해당 태그가 선택된 리뷰 수", example = "5400")
	private Long reviewCount;

	/**
	 * Repository 쿼리 결과(Object[])를 DTO로 변환
	 */
	public static ReviewTagStatisticsResponseDTO from(Object[] result) {
		return ReviewTagStatisticsResponseDTO.builder()
			.tagId((Long) result[0])
			.tagName((String) result[1])
			.reviewCount((Long) result[2])
			.build();
	}
}
