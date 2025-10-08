package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "피드백 요청 일별")
public class DailyFeedbackSummaryDTO {

	@Schema(description = "일")
	private Integer day;

	@Schema(description = "해당 주의 피드백 요청 수")
	private Integer totalCount;

	@Schema(description = "해당 월에 읽지 않은 피드백 답변이 존재하는지 여부")
	private Boolean hasUnreadFeedbackResponse;

	@Schema(description = "해당 월에 트레이너 답변 대기 상태의 요청이 존재하는지 여부")
	private Boolean hasPendingTrainerResponse;

}
