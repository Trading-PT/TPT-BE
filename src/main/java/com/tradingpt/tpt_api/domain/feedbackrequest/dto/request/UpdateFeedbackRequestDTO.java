package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 피드백 요청 수정 DTO
 *
 * 수정 불가 필드 (생성 시 결정):
 * - investmentType: 투자 타입
 * - courseStatus: 완강 여부
 * - feedbackYear, feedbackMonth, feedbackWeek: 날짜 정보 (feedbackRequestDate에서 자동 계산)
 * - screenshotFiles: 별도 API로 관리
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "피드백 요청 수정 DTO")
public class UpdateFeedbackRequestDTO {

	// ========================================
	// 매매 기본 정보 (필수)
	// ========================================

	@NotBlank(message = "종목은 필수입니다.")
	@Schema(description = "종목", requiredMode = Schema.RequiredMode.REQUIRED)
	private String category;

	@Schema(description = "포지션 홀딩 시간")
	private String positionHoldingTime;

	@NotNull(message = "포지션은 필수입니다.")
	@Schema(description = "포지션 (LONG/SHORT)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Position position;

	@NotNull(message = "P&L은 필수입니다.")
	@Schema(description = "P&L", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal pnl;

	@NotNull(message = "전체 자산 기준 P&L은 필수입니다.")
	@Schema(description = "전체 자산 기준 P&L", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal totalAssetPnl;

	@NotNull(message = "손익비는 필수입니다.")
	@Schema(description = "손익비 (R&R)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Double rnr;

	@NotNull(message = "리스크 테이킹은 필수입니다.")
	@Schema(description = "리스크 테이킹", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal riskTaking;

	@NotNull(message = "레버리지는 필수입니다.")
	@DecimalMin(value = "1.0", message = "레버리지는 1.0 이상이어야 합니다.")
	@DecimalMax(value = "125.0", message = "레버리지는 125.0 이하여야 합니다.")
	@Schema(description = "레버리지 (1.0-125.0)", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal leverage;

	// ========================================
	// 매매 상세 정보 (필수)
	// ========================================

	@NotNull(message = "비중은 필수입니다.")
	@Min(value = 1, message = "비중은 1 이상이어야 합니다.")
	@Max(value = 100, message = "비중은 100 이하여야 합니다.")
	@Schema(description = "비중 (운용 자금 대비, 1-100)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer operatingFundsRatio;

	@NotNull(message = "진입 가격은 필수입니다.")
	@Schema(description = "진입 가격", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal entryPrice;

	@NotNull(message = "탈출 가격은 필수입니다.")
	@Schema(description = "탈출 가격", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal exitPrice;

	@NotNull(message = "설정 손절가는 필수입니다.")
	@Schema(description = "설정 손절가", requiredMode = Schema.RequiredMode.REQUIRED)
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
	// 완강 후 전용 필드 (선택 - 완강 후인 경우만 사용)
	// ========================================

	@Schema(description = "디렉션 프레임 존재 여부")
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
	// SWING 전용 필드 (선택 - SWING인 경우만 사용)
	// ========================================

	@Schema(description = "포지션 시작 날짜 (SWING 전용)")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate positionStartDate;

	@Schema(description = "포지션 종료 날짜 (SWING 전용)")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate positionEndDate;

	// ========================================
	// Validation
	// ========================================

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
}
