package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "트레이너 작성 피드백 일괄 업데이트 요청 DTO")
public class UpdateTrainerWrittenRequestDTO {

	@Schema(
		description = "트레이너 작성으로 설정할 피드백 요청 ID 목록 (개수 제한 없음)",
		example = "[1, 2, 3, 5, 8]",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	@NotEmpty(message = "피드백 요청 ID 목록은 비어있을 수 없습니다.")
	private List<Long> feedbackRequestIds;
}
