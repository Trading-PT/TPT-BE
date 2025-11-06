package com.tradingpt.tpt_api.domain.column.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ColumnCreateRequestDTO {

    @Schema(description = "칼럼 제목", example = "ETF란 무엇인가요?")
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @Schema(description = "칼럼 부제목", example = "ETF란 무엇인가요에 대한 내용입니다.")
    @NotBlank(message = "부제목은 필수 입력값입니다.")
    private String subtitle;

    @Schema(description = "칼럼 내용", example = "ETF는 상장지수펀드로, 주식처럼 거래되는 펀드입니다...")
    @NotBlank(message = "내용은 필수 입력값입니다.")
    private String content;

    @Schema(description = "칼럼 썸네일 이미지", example = "https:dfjkdafad")
    private String thumbnailImage;

    @Schema(description = "칼럼 카테고리", example = "ETF")
    @NotBlank(message = "카테고리는 필수 입력값입니다.")
    private String category;

    @Schema(description = "작성자 이름", example = "홍길동")
    @NotBlank(message = "작성자 이름은 필수 입력값입니다.")
    private String writerName;
}
