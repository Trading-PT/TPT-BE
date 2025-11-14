package com.tradingpt.tpt_api.domain.subscriptionplan.dto.request;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 플랜 생성 요청 DTO
 */
@Getter
@NoArgsConstructor
@Schema(description = "구독 플랜 생성 요청 DTO")
public class SubscriptionPlanCreateRequestDTO {

	@NotBlank(message = "플랜 이름은 필수입니다.")
	@Schema(description = "플랜 이름", example = "기본 구독 플랜", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@NotNull(message = "구독료는 필수입니다.")
	@DecimalMin(value = "0.0", inclusive = false, message = "구독료는 0보다 커야 합니다.")
	@Schema(description = "월 구독료 (원)", example = "99000", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal price;

}
