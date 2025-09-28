package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
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
@Schema(description = "데이 트레이딩 피드백 요청 상세 응답 DTO")
public class DayFeedbackRequestDetailResponseDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "피드백 타입")
	private FeedbackType feedbackType;

	@Schema(description = "완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "피드백 요청 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 요청 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 요청 주차")
	private Integer feedbackWeek;

	@Schema(description = "날짜")
	private LocalDate feedbackRequestedAt;

	@Schema(description = "피드백 상태")
	private Status status;

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
	private LocalDate entryPoint2;

	@Schema(description = "매매 복기")
	private String tradingReview;

	public static DayFeedbackRequestDetailResponseDTO of(DayRequestDetail dayRequest) {
		return DayFeedbackRequestDetailResponseDTO.builder()
			.id(dayRequest.getId())
			.feedbackType(dayRequest.getFeedbackType())
			.status(dayRequest.getStatus())
			.feedbackRequestedAt(dayRequest.getFeedbackRequestedAt())
			.courseStatus(dayRequest.getCourseStatus())
			.feedbackYear(dayRequest.getFeedbackYear())
			.feedbackMonth(dayRequest.getFeedbackMonth())
			.feedbackWeek(dayRequest.getFeedbackWeek())
			.isBestFeedback(dayRequest.getIsBestFeedback())
			.updatedAt(dayRequest.getUpdatedAt())
			.category(dayRequest.getCategory())
			.positionHoldingTime(dayRequest.getPositionHoldingTime())
			.screenshotImageUrls(
				dayRequest.getFeedbackRequestAttachments().stream()
					.map(FeedbackRequestAttachment::getFileUrl)
					.toList()
			)
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
			.tradingReview(dayRequest.getTradingReview())
			.build();
	}
}