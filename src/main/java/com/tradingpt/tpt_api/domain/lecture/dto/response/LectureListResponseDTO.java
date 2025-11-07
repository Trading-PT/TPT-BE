package com.tradingpt.tpt_api.domain.lecture.dto;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "강의 목록 응답 DTO")
public class LectureListResponseDTO {

    @Schema(description = "강의 ID", example = "15")
    private Long lectureId;

    @Schema(description = "챕터 ID", example = "3")
    private Long chapterId;

    @Schema(description = "강의 제목", example = "Chapter 2. 주린이가 시장에서 살아남는 방법")
    private String title;

    @Schema(description = "업로더 이름", example = "김개똥")
    private String trainerName;

    @Schema(description = "첨부파일 존재 여부", example = "true")
    private boolean hasAttachments;

    @Schema(description = "공개 범위", example = "SUBSCRIBER_WEEKLY")
    private LectureExposure lectureExposure;

    @Schema(description = "업로드 시각", example = "2025-06-15T08:00:00")
    private LocalDateTime createdAt;

    public static LectureListResponseDTO from(Lecture lecture) {
        return LectureListResponseDTO.builder()
                .lectureId(lecture.getId())
                .chapterId(lecture.getChapter().getId())
                .title(lecture.getTitle())
                .trainerName(lecture.getTrainer().getName())
                .hasAttachments(lecture.getAttachments() != null && !lecture.getAttachments().isEmpty())
                .lectureExposure(lecture.getLectureExposure())
                .createdAt(lecture.getCreatedAt())
                .build();
    }
}
