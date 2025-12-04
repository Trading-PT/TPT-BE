package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 피드백 요청 생성 통합 DTO
 * DAY, SWING 타입을 단일 DTO로 통합 관리
 * - investmentType 필드로 타입 구분
 * - 타입별 전용 필드는 검증 로직으로 관리
 */
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "피드백 요청 생성 DTO (DAY/SWING 통합)")
public class CreateFeedbackRequestDTO {

	// ========================================
	// 타입 구분 필드
	// ========================================

	@NotNull(message = "투자 타입은 필수입니다.")
	@Schema(description = "투자 타입 (DAY/SWING)", requiredMode = Schema.RequiredMode.REQUIRED)
	private InvestmentType investmentType;

	// ========================================
	// 공통 필드 (완강 전/후 모두 사용)
	// ========================================

	@NotNull(message = "완강 여부는 필수입니다.")
	@Schema(description = "완강 여부", requiredMode = Schema.RequiredMode.REQUIRED)
	private CourseStatus courseStatus;

	@NotNull(message = "피드백 요청 연도는 필수입니다.")
	@Min(value = 2020, message = "연도는 2020년 이상이어야 합니다.")
	@Max(value = 2100, message = "연도는 2100년 이하여야 합니다.")
	@Schema(description = "피드백 요청 연도", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer feedbackYear;

	@NotNull(message = "피드백 요청 월은 필수입니다.")
	@Min(value = 1, message = "월은 1 이상이어야 합니다.")
	@Max(value = 12, message = "월은 12 이하여야 합니다.")
	@Schema(description = "피드백 요청 월", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer feedbackMonth;

	@NotNull(message = "피드백 요청 주차는 필수입니다.")
	@Min(value = 1, message = "주차는 1 이상이어야 합니다.")
	@Max(value = 5, message = "주차는 5 이하여야 합니다.")
	@Schema(description = "피드백 요청 주차", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer feedbackWeek;

	@NotNull(message = "요청 날짜는 필수입니다.")
	@PastOrPresent(message = "요청 날짜는 오늘을 포함한 과거 날짜만 가능합니다.")
	@Schema(description = "요청 날짜", requiredMode = Schema.RequiredMode.REQUIRED)
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate feedbackRequestDate;

	@NotBlank(message = "종목은 필수입니다.")
	@Schema(description = "종목", requiredMode = Schema.RequiredMode.REQUIRED)
	private String category;

	@Schema(description = "포지션 홀딩 시간")
	private String positionHoldingTime;

	@Schema(description = "스크린샷 이미지 파일")
	@NotNull(message = "스크린샷 하나 이상은 필수입니다.")
	private List<MultipartFile> screenshotFiles;

	@NotNull(message = "리스크 테이킹은 필수입니다.")
	@Schema(description = "리스크 테이킹", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal riskTaking;

	@NotNull(message = "레버리지는 필수입니다.")
	@DecimalMin(value = "1.0", message = "레버리지는 1.0 이상이어야 합니다.")
	@DecimalMax(value = "125.0", message = "레버리지는 125.0 이하여야 합니다.")
	@Schema(description = "레버리지 (1.0-125.0)", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal leverage;

	@NotNull(message = "포지션은 필수입니다.")
	@Schema(description = "포지션 (LONG/SHORT)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Position position;

	@NotNull(message = "P&L은 필수입니다.")
	@Schema(description = "P&L", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal pnl;

	@NotNull(message = "전체 자산 기준 P&L은 필수입니다.")
	@Schema(description = "전체 자산 기준 P&L (pnl * 비중 / 100)", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal totalAssetPnl;

	@NotNull(message = "손익비는 필수입니다.")
	@Schema(description = "손익비 (R&R)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Double rnr;

	@Schema(description = "매매 복기")
	private String tradingReview;

	@Schema(description = "토큰 사용 여부", example = "true")
	private Boolean useToken;

	// ========================================
	// 공통 필드 (완강 전/후 모두 필수 또는 선택)
	// ========================================

	@NotNull(message = "비중은 필수입니다.")
	@Min(value = 1, message = "비중은 1 이상이어야 합니다.")
	@Max(value = 100, message = "비중은 100 이하여야 합니다.")
	@Schema(description = "비중 (운용 자금 대비, 1-100) - 필수", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer operatingFundsRatio;

	@NotNull(message = "진입 가격은 필수입니다.")
	@Schema(description = "진입 가격 - 필수", example = "50000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal entryPrice;

	@NotNull(message = "탈출 가격은 필수입니다.")
	@Schema(description = "탈출 가격 - 필수", example = "55000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal exitPrice;

	@NotNull(message = "설정 손절가는 필수입니다.")
	@Schema(description = "설정 손절가 - 필수", example = "48000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal settingStopLoss;

	@Schema(description = "설정 익절가 - 선택", example = "60000.00")
	private BigDecimal settingTakeProfit;

	// ========================================
	// 완강 전 전용 필드
	// ========================================

	@Schema(description = "포지션 진입 근거 - 완강 전 필수")
	private String positionStartReason;

	@Schema(description = "포지션 탈출 근거 - 완강 전 필수")
	private String positionEndReason;

	// ========================================
	// 완강 후 전용 필드 (DAY/SWING 공통)
	// ========================================

	@Schema(description = "디렉션 프레임 존재 여부 - 완강 후 필수")
	private Boolean directionFrameExists;

	@Schema(description = "디렉션 프레임 - 완강 후 필수")
	private String directionFrame;

	@Schema(description = "메인 프레임 - 완강 후 필수")
	private String mainFrame;

	@Schema(description = "서브 프레임 - 완강 후 필수")
	private String subFrame;

	@Schema(description = "추세 분석 - 완강 후 필수")
	private String trendAnalysis;

	@Schema(description = "담당 트레이너 피드백 요청 사항 - 완강 후 선택")
	private String trainerFeedbackRequestContent;

	@Schema(description = "진입 타점 - 완강 후 필수")
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

	@Schema(description = "포지션 시작 날짜 - SWING 완강 후 필수")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate positionStartDate;

	@Schema(description = "포지션 종료 날짜 - SWING 완강 후 필수")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate positionEndDate;

	// ========================================
	// Validation
	// ========================================

	/**
	 * 피드백 날짜 정보 일치 검증
	 * feedbackYear, feedbackMonth, feedbackWeek가 feedbackRequestDate와 일치하는지 확인
	 */
	@AssertTrue(message = "피드백 연/월/주차 정보가 요청 날짜와 일치하지 않습니다.")
	@JsonIgnore
	public boolean isFeedbackPeriodValid() {
		if (feedbackRequestDate == null || feedbackYear == null
			|| feedbackMonth == null || feedbackWeek == null) {
			return true;
		}

		FeedbackPeriodUtil.FeedbackPeriod calculatedPeriod =
			FeedbackPeriodUtil.resolveFrom(feedbackRequestDate);

		return calculatedPeriod.year() == feedbackYear
			&& calculatedPeriod.month() == feedbackMonth
			&& calculatedPeriod.week() == feedbackWeek;
	}

	// ========================================
	// 완강 전 전용 필드 개별 검증
	// ========================================

	@AssertTrue(message = "포지션 진입 근거 입력이 필요합니다.")
	@JsonIgnore
	public boolean isPositionStartReasonValid() {
		if (courseStatus != CourseStatus.BEFORE_COMPLETION) {
			return true;
		}
		return positionStartReason != null && !positionStartReason.isBlank();
	}

	@AssertTrue(message = "포지션 탈출 근거 입력이 필요합니다.")
	@JsonIgnore
	public boolean isPositionEndReasonValid() {
		if (courseStatus != CourseStatus.BEFORE_COMPLETION) {
			return true;
		}
		return positionEndReason != null && !positionEndReason.isBlank();
	}

	// ========================================
	// 완강 후 전용 필드 개별 검증 (DAY/SWING 공통)
	// ========================================

	@AssertTrue(message = "디렉션 프레임 존재 여부 입력이 필요합니다.")
	@JsonIgnore
	public boolean isDirectionFrameExistsValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		return directionFrameExists != null;
	}

	@AssertTrue(message = "디렉션 프레임 입력이 필요합니다.")
	@JsonIgnore
	public boolean isDirectionFrameValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		return directionFrame != null && !directionFrame.isBlank();
	}

	@AssertTrue(message = "메인 프레임 입력이 필요합니다.")
	@JsonIgnore
	public boolean isMainFrameValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		return mainFrame != null && !mainFrame.isBlank();
	}

	@AssertTrue(message = "서브 프레임 입력이 필요합니다.")
	@JsonIgnore
	public boolean isSubFrameValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		return subFrame != null && !subFrame.isBlank();
	}

	@AssertTrue(message = "추세 분석 입력이 필요합니다.")
	@JsonIgnore
	public boolean isTrendAnalysisValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		return trendAnalysis != null && !trendAnalysis.isBlank();
	}

	@AssertTrue(message = "진입 타점 입력이 필요합니다.")
	@JsonIgnore
	public boolean isEntryPointValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		return entryPoint != null;
	}

	// ========================================
	// SWING 전용 필드 개별 검증
	// ========================================

	@AssertTrue(message = "포지션 시작 날짜 입력이 필요합니다.")
	@JsonIgnore
	public boolean isPositionStartDateValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		if (investmentType != InvestmentType.SWING) {
			return true;
		}
		return positionStartDate != null;
	}

	@AssertTrue(message = "포지션 종료 날짜 입력이 필요합니다.")
	@JsonIgnore
	public boolean isPositionEndDateValid() {
		if (courseStatus != CourseStatus.AFTER_COMPLETION) {
			return true;
		}
		if (investmentType != InvestmentType.SWING) {
			return true;
		}
		return positionEndDate != null;
	}

	/**
	 * Entry/Exit Price와 PNL/Position 논리적 일관성 검증
	 *
	 * 롱 포지션 (LONG):
	 * - PNL > 0 (수익) → Exit Price > Entry Price
	 * - PNL < 0 (손실) → Exit Price < Entry Price
	 *
	 * 숏 포지션 (SHORT):
	 * - PNL > 0 (수익) → Exit Price < Entry Price
	 * - PNL < 0 (손실) → Exit Price > Entry Price
	 */
	@AssertTrue(message = "진입/탈출 가격과 PNL/포지션의 논리적 일관성이 맞지 않습니다.")
	@JsonIgnore
	public boolean isEntryExitPriceConsistent() {
		if (position == null || entryPrice == null || exitPrice == null || pnl == null) {
			return true;
		}

		int priceComparison = exitPrice.compareTo(entryPrice);
		int pnlComparison = pnl.compareTo(BigDecimal.ZERO);

		if (position == Position.LONG) {
			if (pnlComparison > 0) {
				return priceComparison > 0;
			} else if (pnlComparison < 0) {
				return priceComparison < 0;
			}
		} else if (position == Position.SHORT) {
			if (pnlComparison > 0) {
				return priceComparison < 0;
			} else if (pnlComparison < 0) {
				return priceComparison > 0;
			}
		}

		return true;
	}

	/**
	 * Stop Loss와 Entry Price, Position 논리적 일관성 검증
	 *
	 * 롱 포지션 (LONG):
	 * - Stop Loss < Entry Price (손절가는 진입가보다 낮아야 함)
	 *
	 * 숏 포지션 (SHORT):
	 * - Stop Loss > Entry Price (손절가는 진입가보다 높아야 함)
	 */
	@AssertTrue(message = "스탑로스 가격이 포지션과 논리적으로 일치하지 않습니다. (LONG: 스탑로스 < 진입가, SHORT: 스탑로스 > 진입가)")
	@JsonIgnore
	public boolean isStopLossConsistent() {
		if (position == null || entryPrice == null || settingStopLoss == null) {
			return true;
		}

		int stopLossVsEntry = settingStopLoss.compareTo(entryPrice);

		if (position == Position.LONG) {
			// LONG: Stop Loss는 Entry Price보다 낮아야 함
			return stopLossVsEntry < 0;
		} else if (position == Position.SHORT) {
			// SHORT: Stop Loss는 Entry Price보다 높아야 함
			return stopLossVsEntry > 0;
		}

		return true;
	}

	// ========================================
	// Helper Methods
	// ========================================

	/**
	 * DAY 타입 여부 확인
	 */
	@JsonIgnore
	public boolean isDay() {
		return this.investmentType == InvestmentType.DAY;
	}

	/**
	 * SWING 타입 여부 확인
	 */
	@JsonIgnore
	public boolean isSwing() {
		return this.investmentType == InvestmentType.SWING;
	}

	/**
	 * 완강 전 여부 확인
	 */
	@JsonIgnore
	public boolean isBeforeCompletion() {
		return this.courseStatus == CourseStatus.BEFORE_COMPLETION;
	}

	/**
	 * 완강 후 여부 확인
	 */
	@JsonIgnore
	public boolean isAfterCompletion() {
		return this.courseStatus == CourseStatus.AFTER_COMPLETION;
	}
}
