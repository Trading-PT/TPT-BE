package com.tradingpt.tpt_api.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "ID 찾기 요청 DTO")
public class FindIdRequest {
    @Email
    @NotBlank
    private String email;
}
