package com.tradingpt.tpt_api.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyCodeRequestDTO {

    @Schema(description = "인증 대상 타입", example = "EMAIL or PHONE")
    @NotBlank(message = "type이 필요합니다.")
    private String type;   // EMAIL or PHONE

    @Schema(description = "검증할 대상 값 (이메일 또는 전화번호)", example = "eun08734@gmail.com")
    @NotBlank(message = "value가 필요합니다.")
    private String value;  // EMAIL이면 이메일, PHONE이면 전화번호

    @Schema(description = "인증번호(6자리)", example = "123456")
    @NotBlank(message = "인증번호를 입력해주세요.")
    private String code;
}
