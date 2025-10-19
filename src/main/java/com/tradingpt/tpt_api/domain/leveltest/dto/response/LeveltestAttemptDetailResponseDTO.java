package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "레벨테스트 시도 상세 조회 DTO")
public class LeveltestAttemptDetailResponseDTO {

    @Schema(description = "시도(Attempt) ID", example = "12")
    private Long attemptId;

    @Schema(description = "총점", example = "85")
    private Integer totalScore;

    @Schema(description = "등급", example = "A")
    private String grade;

    @Schema(description = "응시자 ID", example = "101")
    private Long customerId;

    @Schema(description = "문항별 응답 리스트")
    private List<QuestionResponse> responses;

    @Getter
    @Builder
    @Schema(description = "개별 문항 응답 정보")
    public static class QuestionResponse {

        @Schema(description = "문항 ID", example = "205")
        private Long questionId;

        @Schema(description = "문제 내용", example = "이동평균선의 정의는 무엇인가요?")
        private String content;

        @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE / SHORT_ANSWER / SUBJECTIVE")
        private String problemType;

        @Schema(description = "문제 이미지 URL", example = "https://s3.bucket.com/leveltest/q205.png")
        private String imageUrl;

        @Schema(description = "객관식일 경우 선택지 내용")
        private List<String> choices;

        @Schema(description = "유저가 선택한 번호 (객관식일 경우)", example = "2")
        private String choiceNumber;

        @Schema(description = "유저가 작성한 답변 (주관식/단답형일 경우)", example = "이동평균선은 일정기간 주가의 평균값입니다.")
        private String answerText;

        @Schema(description = "획득 점수", example = "5")
        private Integer scoreAwarded;

        @Schema(description = "정답 여부 (객관식일 경우)", example = "true")
        private Boolean isCorrect;
    }
}
