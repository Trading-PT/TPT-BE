package com.tradingpt.tpt_api.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SendEmailCodeRequestDTO {

    @Schema(
            description = "인증 코드를 전송할 이메일 주소",
            example = "eun08734@gmail.com"
    )
    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;
}
