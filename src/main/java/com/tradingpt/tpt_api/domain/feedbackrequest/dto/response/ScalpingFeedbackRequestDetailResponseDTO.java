package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
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

	@Schema(description = "하루 매매 횟수")
	private Integer dailyTradingCount;

	@Schema(description = "스크린샷 이미지 URL")
	private List<String> screenshotImageUrls;

	@Schema(description = "초기 설정 리스크")
	private Integer riskTaking;

	@Schema(description = "레버리지")
	private Integer leverage;

	@Schema(description = "총 포지션을 잡은 횟수")
	private Integer totalPositionTakingCount;

	@Schema(description = "총 매매 횟수 대비 수익 매매 횟수")
	private Integer totalProfitMarginPerTrades;

	@Schema(description = "스켈핑 시 포지션을 진입하는 근거")
	private String positionStartReason;

	@Schema(description = "스켈핑 시 포지션을 종료하는 근거")
	private String positionEndReason;

	@Schema(description = "담당 트레이너에게 피드백 요청 사항")
	private String trainerFeedbackRequestContent;

	@Schema(description = "15분봉 기준 추세 분석")
	private String trendAnalysis;

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
			.dailyTradingCount(scalpingRequest.getDailyTradingCount())
			.screenshotImageUrls(
				scalpingRequest.getFeedbackRequestAttachments().stream()
					.map(FeedbackRequestAttachment::getFileUrl)
					.toList()
			)
			.riskTaking(scalpingRequest.getRiskTaking())
			.leverage(scalpingRequest.getLeverage())
			.totalPositionTakingCount(scalpingRequest.getTotalPositionTakingCount())
			.totalProfitMarginPerTrades(scalpingRequest.getTotalProfitMarginPerTrades())
			.positionStartReason(scalpingRequest.getPositionStartReason())
			.positionEndReason(scalpingRequest.getPositionEndReason())
			.trainerFeedbackRequestContent(scalpingRequest.getTrainerFeedbackRequestContent())
			.trendAnalysis(scalpingRequest.getTrendAnalysis())
			.build();
	}
}
