package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "레벨테스트 시도 이력 응답 DTO")
public class AdminLeveltestAttemptHistoryResponseDTO {

    @Schema(description = "레벨테스트 시도 ID", example = "12")
    private Long attemptId;

    @Schema(description = "시도 회차 (1회차, 2회차...)", example = "3")
    private int attemptOrder;

    @Schema(description = "응시일", example = "2025-12-28")
    private LocalDate testDate;

    @Schema(description = "등급(A/B/C)", example = "B")
    private String grade;

    @Schema(description = "채점 트레이너 이름", example = "이직원")
    private String gradingTrainerName;

    @Schema(description = "담당 트레이너 이름(없으면 '-')", example = "김트니")
    private String assignedTrainerName;


    /**
     * LevelTestAttempt → DTO 변환
     */
    public static AdminLeveltestAttemptHistoryResponseDTO from(
            LevelTestAttempt attempt,
            int attemptOrder,
            String gradingTrainerName,
            String assignedTrainerName
    ) {
        return AdminLeveltestAttemptHistoryResponseDTO.builder()
                .attemptId(attempt.getId())
                .attemptOrder(attemptOrder)
                .testDate(attempt.getCreatedAt().toLocalDate())
                .grade(attempt.getGrade() != null ? attempt.getGrade().name() : null)
                .gradingTrainerName(gradingTrainerName)
                .assignedTrainerName(assignedTrainerName)
                .build();
    }
}
