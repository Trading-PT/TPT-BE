package com.tradingpt.tpt_api.domain.lecture.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "특정 회원의 PRO 강의 과제 현황 요약 응답")
public class CustomerHomeworkSummaryResponseDTO {

    @Schema(description = "고객 ID", example = "123")
    private Long customerId;

    @Schema(description = "고객 이름", example = "홍길동")
    private String customerName;

    @Schema(description = "지금까지 열린 PRO 강의 개수(=주차 수)", example = "3")
    private int totalOpenedCount;

    @Schema(description = "열린 강의 중 미제출 과제 개수", example = "2")
    private int notSubmittedCount;

    @Schema(description = "강의별 과제 현황 목록")
    private List<CustomerHomeworkItemDTO> items;

    // ======================
    // 강의별 과제 요약 DTO
    // ======================
    @Getter
    @Builder
    @Schema(description = "강의별 과제 상태 요약 정보")
    public static class CustomerHomeworkItemDTO {

        @Schema(description = "PRO 강의 ID", example = "45")
        private Long lectureId;

        @Schema(description = "PRO 강의 순번(주차)", example = "1")
        private int order;

        @Schema(description = "강의 제목", example = "1주차: 화면 구성과 기초 개념")
        private String lectureTitle;

        @Schema(
                description = "과제 상태",
                example = "제출",
                allowableValues = {"제출", "미제출", "수강 전"}
        )
        private String status;

        @Schema(description = "이 강의에 대해 제출한 모든 과제 제출 이력 목록")
        private List<SubmissionDTO> submissions;
    }

    // ======================
    // 제출 이력 단건 DTO
    // ======================
    @Getter
    @Builder
    @Schema(description = "해당 강의의 과제 제출 이력 단건 정보")
    public static class SubmissionDTO {

        @Schema(description = "제출 차수 (1 = 최초 제출, 2 = 재제출 ...)", example = "2")
        private int attemptNo;

        @Schema(description = "제출된 PDF 파일명", example = "homework_week1_v2.pdf")
        private String fileName;

        @Schema(description = "제출 파일 다운로드 URL(CloudFront signed URL)",
                example = "https://cdn.example.com/assignments/45/xxx.pdf?Expires=123123&Signature=xxxx")
        private String downloadUrl;

        @Schema(description = "제출 일시", example = "2025-11-29T19:00:01")
        private LocalDateTime submittedAt;
    }
}
