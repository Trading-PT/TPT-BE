package com.tradingpt.tpt_api.domain.column.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ColumnUpdateRequestDTO {

    @Schema(description = "수정할 제목", example = "ETF 기초 가이드(개정판)")
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "칼럼 부제목", example = "ETF란 무엇인가요에 대한 내용입니다.")
    @NotBlank(message = "부제목은 필수 입력값입니다.")
    private String subtitle;

    @Schema(description = "수정할 내용", example = "업데이트된 내용...")
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @Schema(description = "수정할 카테고리", example = "ETF")
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @Schema(description = "작성자 이름", example = "홍길동")
    @NotBlank(message = "작성자 이름은 필수 입력값입니다.")
    private String writerName;
}
