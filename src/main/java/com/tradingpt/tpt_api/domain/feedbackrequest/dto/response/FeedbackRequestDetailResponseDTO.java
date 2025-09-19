package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

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
@Schema(description = "피드백 요청 상세 응답")
public class FeedbackRequestDetailResponseDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "피드백 타입")
	private FeedbackType feedbackType;

	@Schema(description = "피드백 상태")
	private Status status;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "데이 트레이딩 상세", nullable = true)
	private DayFeedbackRequestDetailResponseDTO dayDetail;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "스켈핑 상세", nullable = true)
	private ScalpingFeedbackRequestDetailResponseDTO scalpingDetail;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "스윙 상세", nullable = true)
	private SwingFeedbackRequestDetailResponseDTO swingDetail;
}

