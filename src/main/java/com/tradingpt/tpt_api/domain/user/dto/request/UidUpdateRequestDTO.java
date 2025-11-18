package com.tradingpt.tpt_api.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UidUpdateRequestDTO {

    @NotBlank(message = "uid는 비어 있을 수 없습니다.")
    private String uid;
}
