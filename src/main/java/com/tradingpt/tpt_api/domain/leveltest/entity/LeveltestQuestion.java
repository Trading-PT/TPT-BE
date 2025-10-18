package com.tradingpt.tpt_api.domain.leveltest.entity;

import com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "leveltest_question")
public class LeveltestQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leveltest_question_id")
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false)
    private ProblemType problemType;

    @Column(name = "image_key", length = 512)
    private String imageKey;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "choice_1")
    private String choice1;

    @Column(name = "choice_2")
    private String choice2;

    @Column(name = "choice_3")
    private String choice3;

    @Column(name = "choice_4")
    private String choice4;

    @Column(name = "choice_5")
    private String choice5;

    @Column(name = "correct_choice_num")
    private String correctChoiceNum;

    @Column(name = "answer_text")
    private String answerText;


    /** 이미지 키/URL 교체 (null 허용: 이미지 제거) */
    public void changeImage(String imageKey, String imageUrl) {
        this.imageKey = imageKey;
        this.imageUrl = imageUrl;
    }

    public void changeContent(String content) {
        this.content = content;
    }

    public void changeScore(Integer score) {
        this.score = score;
    }

    public void changeProblemType(ProblemType problemType) {
        this.problemType = problemType;
    }

    /** 객관식 선택지 일괄 변경 (null 허용) */
    public void changeChoices(String choice1, String choice2, String choice3, String choice4, String choice5) {
        this.choice1 = choice1;
        this.choice2 = choice2;
        this.choice3 = choice3;
        this.choice4 = choice4;
        this.choice5 = choice5;
    }

    public void changeCorrectChoiceNum(String correctChoiceNum) {
        this.correctChoiceNum = correctChoiceNum;
    }

    /** 주관식/단답형 정답 변경 (null 허용) */
    public void changeAnswerText(String answerText) {
        this.answerText = answerText;
    }
}
