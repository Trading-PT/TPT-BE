package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "챕터 목록 응답 DTO")
public class ChapterListResponseDTO {

    @Schema(description = "챕터 ID", example = "12")
    private Long chapterId;

    @Schema(description = "챕터명", example = "1주차: 트레이딩 기초")
    private String title;
}
