package com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "투자 유형 변경 신청 승인/거부 요청 DTO")
public class ApproveChangeRequestDTO {

	@Schema(description = "승인 여부", example = "true")
	private Boolean approved;

	@Schema(description = "거부 사유 (거부 시 필수)", example = "현재 완강 전이라 변경이 불가능합니다.")
	private String rejectionReason;
}