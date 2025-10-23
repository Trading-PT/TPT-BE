package com.tradingpt.tpt_api.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "리뷰 작성 요청 DTO")
public class CreateReviewRequestDTO {

	@Schema(description = "리뷰 본문 (HTML/Markdown)")
	@NotBlank
	String content;

}


