package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "스윙 트레이딩 피드백 요청 상세 응답 DTO")
public class SwingFeedbackRequestDetailResponseDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "피드백 타입")
	private FeedbackType feedbackType;

	@Schema(description = "완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "날짜")
	private LocalDate feedbackRequestedAt;

	@Schema(description = "피드백 상태")
	private Status status;

	@Schema(description = "피드백 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 주차")
	private Integer feedbackWeek;

	@Schema(description = "베스트 피드백 여부")
	private Boolean isBestFeedback;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	@Schema(description = "종목")
	private String category;

	@Schema(description = "포지션 홀딩 시간")
	private String positionHoldingTime;

	@Schema(description = "스크린샷 이미지 URL")
	private List<String> screenshotImageUrls;

	@Schema(description = "리스크 테이킹")
	private Integer riskTaking;

	@Schema(description = "레버리지")
	private Integer leverage;

	@Schema(description = "포지션 진입 날짜")
	private LocalDate positionStartDate;

	@Schema(description = "포지션 종료 날짜")
	private LocalDate positionEndDate;

	@Schema(description = "포지션")
	private Position position;

	@Schema(description = "포지션 진입 근거")
	private String positionStartReason;

	@Schema(description = "포지션 탈출 근거")
	private String positionEndReason;

	@Schema(description = "담당 트레이너 피드백 요청 사항")
	private String trainerFeedbackRequestContent;

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

	@Schema(description = "R&R")
	private Double rnr;

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

	public static SwingFeedbackRequestDetailResponseDTO of(SwingRequestDetail swingRequest) {
		return SwingFeedbackRequestDetailResponseDTO.builder()
			.id(swingRequest.getId())
			.createdAt(swingRequest.getCreatedAt())
			.feedbackType(swingRequest.getFeedbackType())
			.courseStatus(swingRequest.getCourseStatus())
			.feedbackRequestedAt(swingRequest.getFeedbackRequestedAt())
			.status(swingRequest.getStatus())
			.feedbackYear(swingRequest.getFeedbackYear())
			.feedbackMonth(swingRequest.getFeedbackMonth())
			.feedbackWeek(swingRequest.getFeedbackWeek())
			.isBestFeedback(swingRequest.getIsBestFeedback())
			.updatedAt(swingRequest.getUpdatedAt())
			.category(swingRequest.getCategory())
			.positionHoldingTime(swingRequest.getPositionHoldingTime())
			.screenshotImageUrls(
				swingRequest.getFeedbackRequestAttachments().stream()
					.map(FeedbackRequestAttachment::getFileUrl)
					.toList()
			)
			.riskTaking(swingRequest.getRiskTaking())
			.leverage(swingRequest.getLeverage())
			.positionStartDate(swingRequest.getPositionStartDate())
			.positionEndDate(swingRequest.getPositionEndDate())
			.position(swingRequest.getPosition())
			.positionStartReason(swingRequest.getPositionStartReason())
			.positionEndReason(swingRequest.getPositionEndReason())
			.trainerFeedbackRequestContent(swingRequest.getTrainerFeedbackRequestContent())
			.directionFrame(swingRequest.getDirectionFrame())
			.mainFrame(swingRequest.getMainFrame())
			.subFrame(swingRequest.getSubFrame())
			.trendAnalysis(swingRequest.getTrendAnalysis())
			.pnl(swingRequest.getPnl())
			.rnr(swingRequest.getRnr())
			.entryPoint1(swingRequest.getEntryPoint1())
			.grade(swingRequest.getGrade())
			.entryPoint2(swingRequest.getEntryPoint2())
			.entryPoint3(swingRequest.getEntryPoint3())
			.tradingReview(swingRequest.getTradingReview())
			.build();
	}

}
