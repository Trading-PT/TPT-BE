package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "데이 트레이딩 피드백 요청 상세 응답 DTO")
public class DayRequestDetailResponse {

    @Schema(description = "피드백 요청 ID")
    private Long id;

    @Schema(description = "고객 ID")
    private Long customerId;

    @Schema(description = "고객 이름")
    private String customerName;

    @Schema(description = "피드백 타입")
    private FeedbackType feedbackType;

    @Schema(description = "피드백 상태")
    private Status status;

    @Schema(description = "피드백 요청 일자")
    private LocalDate feedbackRequestedAt;

    @Schema(description = "완강 여부")
    private Boolean isCourseCompleted;

    @Schema(description = "피드백 연도")
    private Integer feedbackYear;

    @Schema(description = "피드백 월")
    private Integer feedbackMonth;

    @Schema(description = "피드백 주차")
    private Integer feedbackWeek;

    @Schema(description = "베스트 피드백 여부")
    private Boolean isBestFeedback;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    // Day Request Detail specific fields
    @Schema(description = "요청 날짜")
    private LocalDate requestDate;

    @Schema(description = "종목")
    private String category;

    @Schema(description = "포지션 홀딩 시간")
    private String positionHoldingTime;

    @Schema(description = "포지션 진입 날짜")
    private LocalDate positionStartDate;

    @Schema(description = "포지션 종료 날짜")
    private LocalDate positionEndDate;

    @Schema(description = "스크린샷 이미지 URL")
    private String screenshotImageUrl;

    @Schema(description = "리스크 테이킹")
    private Integer riskTaking;

    @Schema(description = "레버리지")
    private Integer leverage;

    @Schema(description = "포지션")
    private Position position;

    @Schema(description = "포지션 진입 근거")
    private String positionStartReason;

    @Schema(description = "포지션 탈출 근거")
    private String positionEndReason;

    @Schema(description = "담당 트레이너 피드백 요청 사항")
    private String trainerFeedbackRequestContent;

    @Schema(description = "디렉션 프레임 방향성 유무")
    private Boolean directionFrameExists;

    @Schema(description = "디렉션 프레임")
    private String directionFrame;

    @Schema(description = "메인 프레임")
    private String mainFrame;

    @Schema(description = "서브 프레임")
    private String subFrame;

    @Schema(description = "추세 분석")
    private String trendAnalysis;

    @Schema(description = "P&L")
    private BigDecimal pnl;

    @Schema(description = "손익비")
    private String winLossRatio;

    @Schema(description = "1차 진입 타점")
    private EntryPoint entryPoint1;

    @Schema(description = "등급")
    private Grade grade;

    @Schema(description = "2차 진입 타점")
    private LocalDateTime entryPoint2;

    @Schema(description = "3차 진입 타점")
    private LocalDateTime entryPoint3;

    @Schema(description = "매매 복기")
    private String tradingReview;

    public static DayRequestDetailResponse of(DayRequestDetail dayRequest) {
        return DayRequestDetailResponse.builder()
                .id(dayRequest.getId())
                .customerId(dayRequest.getCustomer().getId())
                .customerName(dayRequest.getCustomer().getName())
                .feedbackType(dayRequest.getFeedbackType())
                .status(dayRequest.getStatus())
                .feedbackRequestedAt(dayRequest.getFeedbackRequestedAt())
                .isCourseCompleted(dayRequest.getIsCourseCompleted())
                .feedbackYear(dayRequest.getFeedbackYear())
                .feedbackMonth(dayRequest.getFeedbackMonth())
                .feedbackWeek(dayRequest.getFeedbackWeek())
                .isBestFeedback(dayRequest.getIsBestFeedback())
                .createdAt(dayRequest.getCreatedAt())
                .updatedAt(dayRequest.getUpdatedAt())
                .requestDate(dayRequest.getRequestDate())
                .category(dayRequest.getCategory())
                .positionHoldingTime(dayRequest.getPositionHoldingTime())
                .positionStartDate(dayRequest.getPositionStartDate())
                .positionEndDate(dayRequest.getPositionEndDate())
                .screenshotImageUrl(dayRequest.getScreenshotImageUrl())
                .riskTaking(dayRequest.getRiskTaking())
                .leverage(dayRequest.getLeverage())
                .position(dayRequest.getPosition())
                .positionStartReason(dayRequest.getPositionStartReason())
                .positionEndReason(dayRequest.getPositionEndReason())
                .trainerFeedbackRequestContent(dayRequest.getTrainerFeedbackRequestContent())
                .directionFrameExists(dayRequest.getDirectionFrameExists())
                .directionFrame(dayRequest.getDirectionFrame())
                .mainFrame(dayRequest.getMainFrame())
                .subFrame(dayRequest.getSubFrame())
                .trendAnalysis(dayRequest.getTrendAnalysis())
                .pnl(dayRequest.getPnl())
                .winLossRatio(dayRequest.getWinLossRatio())
                .entryPoint1(dayRequest.getEntryPoint1())
                .grade(dayRequest.getGrade())
                .entryPoint2(dayRequest.getEntryPoint2())
                .entryPoint3(dayRequest.getEntryPoint3())
                .tradingReview(dayRequest.getTradingReview())
                .build();
    }
}