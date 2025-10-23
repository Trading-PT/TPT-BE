package com.tradingpt.tpt_api.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateReviewVisibilityRequestDTO {

	@Schema(description = "공개 여부 (true: 공개, false: 비공개)", example = "true")
	@NotNull(message = "공개 여부는 필수입니다")
	Boolean isPublic;

}
