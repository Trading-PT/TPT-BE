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
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

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

@Getter
@Setter // @ModelAttribute 를 사용하려면 Setter가 필요함.
@NoArgsConstructor
@Schema(description = "데이 트레이딩 피드백 요청 DTO")
public class CreateDayRequestDetailRequestDTO {

	// ========================================
	// 공통 필드 (완강 전/후 모두 사용)
	// ========================================

	@NotNull(message = "완강 여부는 필수입니다.")
	@Schema(description = "완강 여부", requiredMode = Schema.RequiredMode.REQUIRED)
	private CourseStatus courseStatus;

	@NotNull(message = "멤버십 레벨은 필수입니다.")
	@Schema(description = "멤버십", requiredMode = Schema.RequiredMode.REQUIRED)
	private MembershipLevel membershipLevel;

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
	@Schema(description = "손익비", requiredMode = Schema.RequiredMode.REQUIRED)
	private Double rnr;

	@Schema(description = "매매 복기")
	private String tradingReview;

	@Schema(description = "토큰 사용 여부 (BASIC 멤버십 전용)", example = "true")
	private Boolean useToken;

	@Schema(description = "사용할 토큰 개수 (기본값: 1)", example = "1")
	private Integer tokenAmount;

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
	// 완강 후 전용 필드
	// ========================================

	@Schema(description = "디렉션 프레임")
	private String directionFrame;

	@Schema(description = "메인 프레임")
	private String mainFrame;

	@Schema(description = "서브 프레임")
	private String subFrame;

	@Schema(description = "디렉션 프레임 방향성 유무")
	private Boolean directionFrameExists;

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
	// Validation
	// ========================================

	/**
	 * 완강 전 필드 검증
	 */
	@AssertTrue(message = "완강 전 요청은 완강 전 필드 입력이 필요합니다.")
	@JsonIgnore
	public boolean isBeforeCompletionFieldsValid() {
		if (courseStatus == CourseStatus.BEFORE_COMPLETION) {
			return positionStartReason != null && !positionStartReason.isBlank()
				&& positionEndReason != null && !positionEndReason.isBlank();
		}
		return true;
	}

	/**
	 * 완강 후 필드 검증
	 */
	@AssertTrue(message = "완강 후 요청은 완강 후 필드 입력이 필요합니다.")
	@JsonIgnore
	public boolean isAfterCompletionFieldsValid() {
		if (courseStatus == CourseStatus.AFTER_COMPLETION) {
			return directionFrameExists != null
				&& directionFrame != null && !directionFrame.isBlank()
				&& mainFrame != null && !mainFrame.isBlank()
				&& subFrame != null && !subFrame.isBlank()
				&& trendAnalysis != null && !trendAnalysis.isBlank()
				&& entryPoint != null;
		}
		return true;
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
		// 필수 필드가 null이면 다른 검증에서 처리되므로 여기서는 통과
		if (position == null || entryPrice == null || exitPrice == null || pnl == null) {
			return true;
		}

		int priceComparison = exitPrice.compareTo(entryPrice);
		int pnlComparison = pnl.compareTo(BigDecimal.ZERO);

		if (position == Position.LONG) {
			// 롱 포지션: PNL > 0이면 Exit > Entry, PNL < 0이면 Exit < Entry
			if (pnlComparison > 0) {
				return priceComparison > 0; // Exit Price > Entry Price
			} else if (pnlComparison < 0) {
				return priceComparison < 0; // Exit Price < Entry Price
			}
		} else if (position == Position.SHORT) {
			// 숏 포지션: PNL > 0이면 Exit < Entry, PNL < 0이면 Exit > Entry
			if (pnlComparison > 0) {
				return priceComparison < 0; // Exit Price < Entry Price
			} else if (pnlComparison < 0) {
				return priceComparison > 0; // Exit Price > Entry Price
			}
		}

		// PNL = 0인 경우 또는 예외 케이스는 통과
		return true;
	}

}
