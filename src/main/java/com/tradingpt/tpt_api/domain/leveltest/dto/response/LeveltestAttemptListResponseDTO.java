package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레벨테스트 시도 목록 조회 DTO")
public class LeveltestAttemptListResponseDTO {

    @Schema(description = "시도(Attempt) ID", example = "12")
    private Long attemptId;

    @Schema(description = "총점 (객관식+주관식 채점 결과 포함)", example = "85")
    private Integer totalScore;

    @Schema(description = "등급 (예: A, B, C)", example = "A")
    private String grade;

    @Schema(description = "생성일시", example = "2025-10-19T10:15:30")
    private String createdAt;

    public static LeveltestAttemptListResponseDTO from(LevelTestAttempt attempt) {
        return LeveltestAttemptListResponseDTO.builder()
                .attemptId(attempt.getId())
                .totalScore(attempt.getTotalScore())
                .grade(attempt.getGrade().toString())
                .createdAt(attempt.getCreatedAt()
                        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
