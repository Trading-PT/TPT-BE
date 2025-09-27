package com.tradingpt.tpt_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
@Schema(description = "비밀번호 변경 요청 DTO")
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[^A-Za-z0-9]).{6,11}$",
            message = "비밀번호는 6~11자이며 숫자와 특수문자를 각각 1개 이상 포함해야 합니다."
    )
    private String newPassword;
}
