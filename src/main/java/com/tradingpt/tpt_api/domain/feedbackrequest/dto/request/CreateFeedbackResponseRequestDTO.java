package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "피드백 답변 생성 요청 DTO")
public class CreateFeedbackResponseRequestDTO {

    @NotBlank(message = "피드백 답변 내용은 필수입니다.")
    @Schema(description = "피드백 답변 내용")
    private String responseContent;
}