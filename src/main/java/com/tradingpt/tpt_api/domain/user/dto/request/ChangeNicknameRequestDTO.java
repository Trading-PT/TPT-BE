package com.tradingpt.tpt_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeNicknameRequestDTO {

    @Schema(description = "변경할 닉네임", example = "헬트레이더123")
    @NotBlank
    private String nickname;
}
