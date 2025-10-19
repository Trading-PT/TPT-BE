package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "레벨테스트 시도 목록 조회 DTO")
public class AdminLeveltestAttemptListResponseDTO {

    @Schema(description = "시도(Attempt) ID", example = "15")
    private Long attemptId;

    @Schema(description = "유저 이름", example = "홍길동")
    private String customerName;

    @Schema(description = "총점 (객관식 채점 결과만 포함된 점수임)", example = "85")
    private Integer totalScore;

    @Schema(description = "레벨테스트 상태", example = "GRADING")
    private LeveltestStaus status;

    @Schema(description = "시도 생성일시", example = "2025-10-19T13:40:21")
    private LocalDateTime createdAt;
}
