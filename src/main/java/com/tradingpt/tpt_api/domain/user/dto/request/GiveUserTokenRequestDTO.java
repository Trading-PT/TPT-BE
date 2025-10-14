package com.tradingpt.tpt_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class GiveUserTokenRequestDTO {

	@Schema(description = "토큰 개수")
	private Integer tokenCount;

}
