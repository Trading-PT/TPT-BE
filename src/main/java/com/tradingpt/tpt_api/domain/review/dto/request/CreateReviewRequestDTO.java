package com.tradingpt.tpt_api.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CreateReviewRequestDTO(

	@Schema(description = "피드백 본문 (HTML/Markdown)")
	String content
	
) {

}
