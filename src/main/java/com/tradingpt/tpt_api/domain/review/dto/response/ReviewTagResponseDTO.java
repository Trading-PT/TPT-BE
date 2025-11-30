package com.tradingpt.tpt_api.domain.review.dto.response;

import com.tradingpt.tpt_api.domain.review.entity.ReviewTag;

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
@Schema(description = "리뷰 태그 응답 DTO")
public class ReviewTagResponseDTO {

	@Schema(description = "태그 ID", example = "1")
	private Long id;

	@Schema(description = "태그 이름", example = "맞춤 상담 300만원 어치의 가치가 있어요")
	private String name;

	@Schema(description = "태그 설명", example = "맞춤형 상담 서비스에 대한 만족도를 표현합니다")
	private String description;

	/**
	 * ReviewTag 엔티티를 DTO로 변환
	 */
	public static ReviewTagResponseDTO from(ReviewTag reviewTag) {
		return ReviewTagResponseDTO.builder()
			.id(reviewTag.getId())
			.name(reviewTag.getName())
			.description(reviewTag.getDescription())
			.build();
	}
}
