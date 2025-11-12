package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest.*;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "베스트 피드백 일괄 업데이트 요청 DTO")
public class UpdateBestFeedbacksRequestDTO {

	// ✅ FeedbackRequest.MAX_BEST_FEEDBACK_COUNT 참조
	@Schema(
		description = "베스트로 선정할 피드백 ID 목록 (최대 " + MAX_BEST_FEEDBACK_COUNT + "개)",
		example = "[1, 5, 10, 15]"
	)
	@NotNull(message = "피드백 ID 목록은 필수입니다.")
	@Size(
		max = MAX_BEST_FEEDBACK_COUNT,
		message = "베스트 피드백은 최대 " + MAX_BEST_FEEDBACK_COUNT + "개까지만 선택할 수 있습니다."
	)
	private List<Long> feedbackIds;
}