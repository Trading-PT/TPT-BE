package com.tradingpt.tpt_api.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordUpdateRequestDTO {

    @Schema(description = "이메일")
    @NotBlank(message = "email이 필요합니다.")
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @Schema(description = "이메일 인증코드")
    @NotBlank(message = "인증코드가 필요합니다.")
    private String code;

    @Schema(description = "새로운 비밀번호")
    @NotBlank(message = "password가 필요합니다.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,11}$",
            message = "비밀번호는 6~11자이며 숫자와 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    private String NewPassword;

    @Schema(description = "새로운 비밀번호 확인")
    @NotBlank(message = "password가 필요합니다.")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,11}$",
            message = "비밀번호는 6~11자이며 숫자와 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    private String NewPasswordCheck;
}
