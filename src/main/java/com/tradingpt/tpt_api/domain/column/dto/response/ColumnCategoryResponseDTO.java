package com.tradingpt.tpt_api.domain.column.dto.response;

import com.tradingpt.tpt_api.domain.column.entity.ColumnCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "칼럼 카테고리 목록 응답 DTO")
public class ColumnCategoryResponseDTO {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "카테고리명", example = "건강")
    private String name;

    @Schema(description = "카테고리 색상", example = "보라색")
    private String color;

    public static ColumnCategoryResponseDTO from(ColumnCategory category) {
        return ColumnCategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .build();
    }
}
