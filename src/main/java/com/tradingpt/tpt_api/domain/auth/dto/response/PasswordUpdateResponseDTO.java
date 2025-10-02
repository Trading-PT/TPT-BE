package com.tradingpt.tpt_api.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PasswordUpdateResponseDTO {

    @Schema(description = "비밀번호 변경된 사용자 ID")
    private Long userId;
}
