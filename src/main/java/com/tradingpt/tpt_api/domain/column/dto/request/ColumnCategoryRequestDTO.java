package com.tradingpt.tpt_api.domain.column.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ColumnCategoryRequestDTO {

    @Schema(description = "카테고리 이름", example = "ETF")
    @NotBlank(message = "카테고리 이름은 필수 입력값입니다.")
    private String name;

    @Schema(description = "카테고리 색상", example = "보라색")
    @NotBlank(message = "카테고리 색상은 필수 입력값입니다.")
    private String color;
}
