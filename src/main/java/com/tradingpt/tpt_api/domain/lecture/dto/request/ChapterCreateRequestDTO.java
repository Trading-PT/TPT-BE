package com.tradingpt.tpt_api.domain.lecture.dto.request;

import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "강의 챕터 생성 요청 DTO")
public class ChapterCreateRequestDTO {

    @NotBlank
    @Schema(description = "챕터 제목", example = "1주차: 기본 개념 다지기")
    private String title;

    @Schema(description = "챕터 설명", example = "이번 주차에서는 핵심 개념과 필수 이론을 다룹니다.")
    private String description;

    @NotNull
    @Schema(description = "챕터 유형 (BASIC: 무료, PRO: 유료)", example = "BASIC")
    private ChapterType chapterType;

    @NotNull
    @Schema(description = "전체 커리큘럼 내 챕터 순서 (1, 2, 3...)", example = "1")
    private Integer chapterOrder;
}
