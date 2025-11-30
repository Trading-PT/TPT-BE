package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "월간 매매일지 트레이너 평가 Upsert 요청 DTO (트레이너용 - 완강 후)")
public class UpsertMonthlyEvaluationRequestDTO {

	@Size(max = 10000, message = "월간 매매 평가는 10000자를 초과할 수 없습니다.")
	@Schema(description = "한 달 간 매매 최종 평가",
		example = "이번 달은 전반적으로 매매 규율을 잘 지켰습니다. 손익비 관리가 크게 향상되었고, 진입 타이밍도 많이 좋아졌습니다.")
	private String monthlyEvaluation;

	@Size(max = 5000, message = "다음 달 목표는 5000자를 초과할 수 없습니다.")
	@Schema(description = "다음 달 목표 성과",
		example = "다음 달에는 승률을 60% 이상으로 올리고, 평균 손익비 1.5:1 이상을 목표로 합니다.")
	private String nextMonthGoal;
}
