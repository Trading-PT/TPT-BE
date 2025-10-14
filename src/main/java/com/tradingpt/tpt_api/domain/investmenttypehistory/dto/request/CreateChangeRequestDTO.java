package com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request;

import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "투자 유형 변경 신청 요청 DTO")
public class CreateChangeRequestDTO {

	@Schema(description = "변경하고자 하는 투자 유형", example = "SWING")
	@NotNull(message = "변경할 투자 유형은 필수입니다.")
	private InvestmentType requestedType;

	@Schema(description = "변경 사유", example = "데이 트레이딩에서 스윙 트레이딩으로 전환하고 싶습니다.")
	private String reason;
}