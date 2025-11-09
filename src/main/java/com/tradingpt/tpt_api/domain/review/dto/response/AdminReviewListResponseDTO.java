package com.tradingpt.tpt_api.domain.review.dto.response;

import java.util.List;

import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

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
@Schema(description = "관리자 리뷰 목록 응답 DTO (무한 스크롤)")
public class AdminReviewListResponseDTO {

	@Schema(description = "리뷰 목록")
	private List<ReviewResponseDTO> reviews;

	@Schema(description = "슬라이스 정보")
	private SliceInfo sliceInfo;

	public static AdminReviewListResponseDTO of(
		List<ReviewResponseDTO> reviews,
		SliceInfo sliceInfo
	) {
		return AdminReviewListResponseDTO.builder()
			.reviews(reviews)
			.sliceInfo(sliceInfo)
			.build();
	}

}
