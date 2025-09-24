package com.tradingpt.tpt_api.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "ID 찾기 응답 DTO")
public class FindIdResponse {

    private String userName;
}
