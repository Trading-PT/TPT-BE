package com.tradingpt.tpt_api.domain.feedbackresponse.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerDTO;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;

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
@Schema(description = "피드백 응답 DTO")
public class FeedbackResponseDTO {

	@Schema(description = "피드백 응답 ID")
	private Long id;

	@Schema(description = "피드백 응답 제목")
	private String title;

	@Schema(description = "피드백 제공 시각")
	private LocalDateTime submittedAt;

	@Schema(description = "트레이너 정보")
	private TrainerDTO trainer;

	@Schema(description = "피드백 응답 내용")
	private String content;

	public static FeedbackResponseDTO of(FeedbackResponse feedbackResponse, Trainer trainer) {
		return FeedbackResponseDTO.builder()
			.id(feedbackResponse.getId())
			.title(feedbackResponse.getTitle())
			.submittedAt(feedbackResponse.getSubmittedAt())
			.trainer(TrainerDTO.from(trainer))
			.content(feedbackResponse.getResponseContent())
			.build();

	}

}
