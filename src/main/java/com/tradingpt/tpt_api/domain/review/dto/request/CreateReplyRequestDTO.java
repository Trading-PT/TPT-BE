package com.tradingpt.tpt_api.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CreateReplyRequestDTO {

	@Schema(description = "리뷰 답변 본문 (HTML/Markdown)")
	@NotBlank
	private String content;
	
}
