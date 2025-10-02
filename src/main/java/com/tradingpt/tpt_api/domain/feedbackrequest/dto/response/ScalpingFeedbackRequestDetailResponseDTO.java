package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
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
@Schema(description = "스켈핑 트레이딩 피드백 요청 상세 응답 DTO")
public class ScalpingFeedbackRequestDetailResponseDTO {

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

	@Schema(description = "초기 설정 리스크")
	private Integer riskTaking;

	@Schema(description = "레버리지")
	private Integer leverage;

	@Schema(description = "포지션 (LONG/SHORT)")
	private Position position;

	@Schema(description = "P&L")
	private BigDecimal pnl;

	@Schema(description = "손익비")
	private Double rnr;

	@Schema(description = "비중 (운용 자금 대비)")
	private Integer operatingFundsRatio;

	@Schema(description = "진입 자금")
	private BigDecimal entryPrice;

	@Schema(description = "탈출 자금")
	private BigDecimal exitPrice;

	@Schema(description = "설정 손절가")
	private BigDecimal settingStopLoss;

	@Schema(description = "설정 익절가")
	private BigDecimal settingTakeProfit;

	@Schema(description = "포지션 진입 근거")
	private String positionStartReason;

	@Schema(description = "포지션 탈출 근거")
	private String positionEndReason;

	@Schema(description = "매매 복기")
	private String tradingReview;

	public static ScalpingFeedbackRequestDetailResponseDTO of(ScalpingRequestDetail scalpingRequest) {
		return ScalpingFeedbackRequestDetailResponseDTO.builder()
			.id(scalpingRequest.getId())
			.createdAt(scalpingRequest.getCreatedAt())
			.feedbackType(scalpingRequest.getFeedbackType())
			.courseStatus(scalpingRequest.getCourseStatus())
			.feedbackYear(scalpingRequest.getFeedbackYear())
			.feedbackMonth(scalpingRequest.getFeedbackMonth())
			.feedbackWeek(scalpingRequest.getFeedbackWeek())
			.feedbackRequestedAt(scalpingRequest.getFeedbackRequestedAt())
			.status(scalpingRequest.getStatus())
			.isBestFeedback(scalpingRequest.getIsBestFeedback())
			.updatedAt(scalpingRequest.getUpdatedAt())
			.category(scalpingRequest.getCategory())
			.positionHoldingTime(scalpingRequest.getPositionHoldingTime())
			.screenshotImageUrls(
				scalpingRequest.getFeedbackRequestAttachments().stream()
					.map(FeedbackRequestAttachment::getFileUrl)
					.toList()
			)
			.riskTaking(scalpingRequest.getRiskTaking())
			.leverage(scalpingRequest.getLeverage())
			.position(scalpingRequest.getPosition())
			.pnl(scalpingRequest.getPnl())
			.rnr(scalpingRequest.getRnr())
			.operatingFundsRatio(scalpingRequest.getOperatingFundsRatio())
			.entryPrice(scalpingRequest.getEntryPrice())
			.exitPrice(scalpingRequest.getExitPrice())
			.settingStopLoss(scalpingRequest.getSettingStopLoss())
			.settingTakeProfit(scalpingRequest.getSettingTakeProfit())
			.positionStartReason(scalpingRequest.getPositionStartReason())
			.positionEndReason(scalpingRequest.getPositionEndReason())
			.tradingReview(scalpingRequest.getTradingReview())
			.build();
	}
}
