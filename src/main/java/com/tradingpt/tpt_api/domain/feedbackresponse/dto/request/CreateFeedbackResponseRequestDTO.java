package com.tradingpt.tpt_api.domain.feedbackresponse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "피드백 답변 생성 요청 DTO")
public class CreateFeedbackResponseRequestDTO {

	@Schema(description = "피드백 제목")
	private String title;

	@Schema(description = "피드백 본문 (HTML/Markdown)")
	private String content;

}