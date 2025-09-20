package com.tradingpt.tpt_api.domain.feedbackresponse.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "피드백 답변 수정 요청 DTO")
public class UpdateFeedbackResponseRequestDTO {

	@Schema(description = "수정할 피드백 제목")
	private String title;

	@Schema(description = "수정할 피드백 답변 내용")
	private String content;
}