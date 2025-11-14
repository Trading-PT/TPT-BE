package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "챕터 블록 DTO (챕터 + 강의 리스트)")
public class ChapterBlockDTO {

    @Schema(description = "챕터 ID", example = "1")
    private Long chapterId;

    @Schema(description = "챕터 이름", example = "Chapter 01. 올바른 트레이딩이란?")
    private String chapterTitle;

    @Schema(description = "챕터 설명", example = "올바른 트레이딩이란 이런것입니다..ㅇ")
    private String description;

    @Schema(description = "전체 강의 진도율 (%)", example = "75")
    private Integer progressPercent;

    @Schema(description = "챕터 타입 (REGULAR=무료, PRO=유료)", example = "REGULAR")
    private String chapterType;

    @Schema(description = "챕터 내 강의 리스트")
    private List<LectureResponseDTO> lectures;
}
