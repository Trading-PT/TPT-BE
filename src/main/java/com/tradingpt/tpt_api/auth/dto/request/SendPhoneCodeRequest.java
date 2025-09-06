package com.tradingpt.tpt_api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SendPhoneCodeRequest {

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String phone;
}
