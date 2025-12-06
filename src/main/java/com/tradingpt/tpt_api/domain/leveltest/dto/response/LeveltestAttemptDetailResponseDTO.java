package com.tradingpt.tpt_api.domain.leveltest.dto.response;

import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestQuestion;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Builder
@Schema(description = "레벨테스트 시도 상세 조회 DTO")
public class LeveltestAttemptDetailResponseDTO {

    @Schema(description = "시도(Attempt) ID", example = "12")
    private Long attemptId;

    @Schema(description = "등급", example = "A")
    private String grade;

    @Schema(description = "응시자 ID", example = "101")
    private Long customerId;

    @Schema(description = "문항별 응답 리스트")
    private List<QuestionResponse> responses;

    /**
     * B안(별도 조회)용 팩토리 메서드
     * - attempt는 시도 메타 정보만 사용
     * - responses는 레포지토리에서 attemptId로 fetch join하여 주입
     */
    public static LeveltestAttemptDetailResponseDTO from(LevelTestAttempt attempt,
                                                         List<LevelTestResponse> responses) {
        return LeveltestAttemptDetailResponseDTO.builder()
                .attemptId(attempt.getId())
                .grade(String.valueOf(attempt.getGrade()))
                .customerId(attempt.getCustomer().getId())
                .responses(
                        responses.stream()
                                .map(QuestionResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

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

        public static QuestionResponse from(LevelTestResponse response) {

            LevelTestQuestion q = response.getLeveltestQuestion();

            return QuestionResponse.builder()
                    .questionId(q.getId())
                    .content(q.getContent())
                    .problemType(q.getProblemType().name())
                    .imageUrl(q.getImageUrl())
                    .choices(
                            Stream.of(q.getChoice1(), q.getChoice2(), q.getChoice3(), q.getChoice4(), q.getChoice5())
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList())
                    )
                    .choiceNumber(response.getChoiceNumber())
                    .answerText(response.getAnswerText())
                    .build();
        }
    }
}
