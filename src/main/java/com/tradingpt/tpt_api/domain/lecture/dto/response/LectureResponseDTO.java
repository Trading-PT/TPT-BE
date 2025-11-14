package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "강의 요약 응답 DTO (무료·유료 통합)")
public class LectureResponseDTO {

    @Schema(description = "강의 ID", example = "12")
    private Long lectureId;

    @Schema(description = "챕터 ID", example = "3")
    private Long chapterId;

    @Schema(description = "강의 제목", example = "트레이딩을 할 준비가 되었는가?")
    private String title;

    @Schema(description = "강의 미리보기 내용 (앞 14글자)", example = "트레이딩을 할...")
    private String content;

    @Schema(description = "유료 여부 (true = 유료 강의)", example = "false")
    private boolean paid;

    // ======================
    // 무료 강의 전용 필드
    // ======================

    @Schema(description = "썸네일 이미지 URL (무료 강의 카드용)", example = "https://bucket.s3/.../thumb.png")
    private String thumbnailUrl;

    @Schema(description = "강의 총 시간 (초 단위)", example = "1320")
    private Integer durationSeconds;

    @Schema(description = "강의 시청에 필요한 토큰 수(무료=0)", example = "0")
    private Integer requiredTokens;

    @Schema(description = "마지막 시청 시각 (무료인경우에도 필요, 이거로 구매 완료 구분)", example = "2025-01-01T12:30:00")
    private LocalDateTime lastWatchedAt;

    @Schema(description = "누적 시청 시간(초)", example = "600")
    private Integer watchedSeconds;

    @Schema(description = "완강 여부", example = "true")
    private Boolean completed;
}
