package com.tradingpt.tpt_api.domain.review.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 작성 요청 DTO")
public class CreateReviewRequestDTO {

	@Schema(description = "리뷰 본문 (HTML/Markdown)")
	@NotBlank
	private String content;

	@Schema(description = "리뷰 별점 (1-5)", example = "5")
	@NotNull(message = "별점은 필수입니다.")
	@Min(value = 1, message = "별점은 1 이상이어야 합니다.")
	@Max(value = 5, message = "별점은 5 이하여야 합니다.")
	private Integer rating;

	@Schema(description = "선택한 리뷰 태그 ID 목록", example = "[1, 3, 5]")
	private List<Long> tagIds;

}


