package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드백 요청 상세 응답 통합 DTO
 * DAY, SWING 타입을 단일 DTO로 통합 관리
 * - investmentType 필드로 타입 구분
 * - 타입별 전용 필드는 nullable로 관리
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "피드백 요청 상세 응답 (DAY/SWING 통합)")
public class FeedbackRequestDetailResponseDTO {

	// ========================================
	// 기본 정보
	// ========================================

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	@Schema(description = "투자 유형 (DAY/SWING)")
	private InvestmentType investmentType;

	@Schema(description = "완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "멤버십 레벨")
	private MembershipLevel membershipLevel;

	@Schema(description = "피드백 상태")
	private Status status;

	@Schema(description = "베스트 피드백 여부")
	private Boolean isBestFeedback;

	// ========================================
	// 날짜 정보
	// ========================================

	@Schema(description = "피드백 요청 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 요청 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 요청 주차")
	private Integer feedbackWeek;

	@Schema(description = "피드백 요청 날짜")
	private LocalDate feedbackRequestDate;

	// ========================================
	// 매매 기본 정보
	// ========================================

	@Schema(description = "종목")
	private String category;

	@Schema(description = "포지션 홀딩 시간")
	private String positionHoldingTime;

	@Schema(description = "스크린샷 이미지 URL 목록")
	private List<String> screenshotImageUrls;

	@Schema(description = "리스크 테이킹")
	private BigDecimal riskTaking;

	@Schema(description = "레버리지")
	private BigDecimal leverage;

	@Schema(description = "포지션 (LONG/SHORT)")
	private Position position;

	@Schema(description = "P&L")
	private BigDecimal pnl;

	@Schema(description = "전체 자산 기준 P&L")
	private BigDecimal totalAssetPnl;

	@Schema(description = "손익비 (R&R)")
	private Double rnr;

	// ========================================
	// 매매 상세 정보
	// ========================================

	@Schema(description = "비중 (운용 자금 대비)")
	private Integer operatingFundsRatio;

	@Schema(description = "진입 가격")
	private BigDecimal entryPrice;

	@Schema(description = "탈출 가격")
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

	// ========================================
	// 완강 후 전용 필드 (DAY/SWING 공통)
	// ========================================

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

	@Schema(description = "담당 트레이너 피드백 요청 사항")
	private String trainerFeedbackRequestContent;

	@Schema(description = "진입 타점")
	private EntryPoint entryPoint;

	@Schema(description = "등급")
	private Grade grade;

	@Schema(description = "추가 매수 횟수")
	private Integer additionalBuyCount;

	@Schema(description = "분할 매도 횟수")
	private Integer splitSellCount;

	// ========================================
	// SWING 전용 필드 (DAY에서는 null)
	// ========================================

	@Schema(description = "포지션 시작 날짜 (SWING 전용)")
	private LocalDate positionStartDate;

	@Schema(description = "포지션 종료 날짜 (SWING 전용)")
	private LocalDate positionEndDate;

	// ========================================
	// 토큰 사용 정보
	// ========================================

	@Schema(description = "토큰 사용 여부")
	private Boolean isTokenUsed;

	@Schema(description = "사용한 토큰 개수")
	private Integer tokenAmount;

	// ========================================
	// 피드백 응답 정보
	// ========================================

	@Schema(description = "피드백 응답")
	private FeedbackResponseDTO feedbackResponse;

	// ========================================
	// Static Factory Method
	// ========================================

	/**
	 * Entity에서 DTO로 변환
	 *
	 * @param feedbackRequest 피드백 요청 엔티티
	 * @return 피드백 요청 상세 응답 DTO
	 */
	public static FeedbackRequestDetailResponseDTO from(FeedbackRequest feedbackRequest) {
		FeedbackResponse response = feedbackRequest.getFeedbackResponse();

		return FeedbackRequestDetailResponseDTO.builder()
			// 기본 정보
			.id(feedbackRequest.getId())
			.createdAt(feedbackRequest.getCreatedAt())
			.updatedAt(feedbackRequest.getUpdatedAt())
			.investmentType(feedbackRequest.getInvestmentType())
			.courseStatus(feedbackRequest.getCourseStatus())
			.membershipLevel(feedbackRequest.getMembershipLevel())
			.status(feedbackRequest.getStatus())
			.isBestFeedback(feedbackRequest.getIsBestFeedback())
			// 날짜 정보
			.feedbackYear(feedbackRequest.getFeedbackYear())
			.feedbackMonth(feedbackRequest.getFeedbackMonth())
			.feedbackWeek(feedbackRequest.getFeedbackWeek())
			.feedbackRequestDate(feedbackRequest.getFeedbackRequestDate())
			// 매매 기본 정보
			.category(feedbackRequest.getCategory())
			.positionHoldingTime(feedbackRequest.getPositionHoldingTime())
			.screenshotImageUrls(
				feedbackRequest.getFeedbackRequestAttachments().stream()
					.map(FeedbackRequestAttachment::getFileUrl)
					.toList()
			)
			.riskTaking(feedbackRequest.getRiskTaking())
			.leverage(feedbackRequest.getLeverage())
			.position(feedbackRequest.getPosition())
			.pnl(feedbackRequest.getPnl())
			.totalAssetPnl(feedbackRequest.getTotalAssetPnl())
			.rnr(feedbackRequest.getRnr())
			// 매매 상세 정보
			.operatingFundsRatio(feedbackRequest.getOperatingFundsRatio())
			.entryPrice(feedbackRequest.getEntryPrice())
			.exitPrice(feedbackRequest.getExitPrice())
			.settingStopLoss(feedbackRequest.getSettingStopLoss())
			.settingTakeProfit(feedbackRequest.getSettingTakeProfit())
			.positionStartReason(feedbackRequest.getPositionStartReason())
			.positionEndReason(feedbackRequest.getPositionEndReason())
			.tradingReview(feedbackRequest.getTradingReview())
			// 완강 후 필드
			.directionFrameExists(feedbackRequest.getDirectionFrameExists())
			.directionFrame(feedbackRequest.getDirectionFrame())
			.mainFrame(feedbackRequest.getMainFrame())
			.subFrame(feedbackRequest.getSubFrame())
			.trendAnalysis(feedbackRequest.getTrendAnalysis())
			.trainerFeedbackRequestContent(feedbackRequest.getTrainerFeedbackRequestContent())
			.entryPoint(feedbackRequest.getEntryPoint())
			.grade(feedbackRequest.getGrade())
			.additionalBuyCount(feedbackRequest.getAdditionalBuyCount())
			.splitSellCount(feedbackRequest.getSplitSellCount())
			// SWING 전용 필드
			.positionStartDate(feedbackRequest.getPositionStartDate())
			.positionEndDate(feedbackRequest.getPositionEndDate())
			// 토큰 정보
			.isTokenUsed(feedbackRequest.getIsTokenUsed())
			.tokenAmount(feedbackRequest.getTokenAmount())
			// 피드백 응답
			.feedbackResponse(response != null ? FeedbackResponseDTO.from(response) : null)
			.build();
	}
}
