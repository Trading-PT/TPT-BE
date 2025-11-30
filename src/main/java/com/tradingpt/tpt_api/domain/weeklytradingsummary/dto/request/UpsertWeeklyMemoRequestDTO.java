package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "주간 매매일지 메모 Upsert 요청 DTO (고객용 - 완강 전)")
public class UpsertWeeklyMemoRequestDTO {

	@Size(max = 5000, message = "메모는 5000자를 초과할 수 없습니다.")
	@Schema(description = "나의 문제점 메모하기",
		example = "이번 주는 손절을 너무 늦게 했습니다. 다음 주에는 손절 라인을 미리 정해두고 매매해야겠습니다.")
	private String memo;
}
