package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Setter
@NoArgsConstructor
@Schema(description = "스캘핑 피드백 요청 DTO")
public class CreateScalpingRequestDetailRequestDTO {

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
	@DecimalMin(value = "1.0", message = "리스크 테이킹은 1.0 이상이어야 합니다.")
	@DecimalMax(value = "100.0", message = "리스크 테이킹은 100.0 이하여야 합니다.")
	@Schema(description = "리스크 테이킹 (1.0-100.0)", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal riskTaking;

	@NotNull(message = "레버리지는 필수입니다.")
	@DecimalMin(value = "1.0", message = "레버리지는 1.0 이상이어야 합니다.")
	@DecimalMax(value = "1000.0", message = "레버리지는 1000.0 이하여야 합니다.")
	@Schema(description = "레버리지 (1.0-1000.0)", requiredMode = Schema.RequiredMode.REQUIRED)
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
	// ⭐ 스캘핑 특수 필드 (완강 전/후 모두 필수)
	// ========================================

	@NotNull(message = "비중은 필수입니다.")
	@Min(value = 1, message = "비중은 1 이상이어야 합니다.")
	@Max(value = 100, message = "비중은 100 이하여야 합니다.")
	@Schema(description = "비중 (운용 자금 대비) - 항상 필수", example = "50", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer operatingFundsRatio;

	@NotNull(message = "진입 가격은 필수입니다.")
	@Schema(description = "진입 가격 - 항상 필수", example = "50000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal entryPrice;

	@NotNull(message = "탈출 가격은 필수입니다.")
	@Schema(description = "탈출 가격 - 항상 필수", example = "55000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal exitPrice;

	@NotNull(message = "설정 손절가는 필수입니다.")
	@Schema(description = "설정 손절가 - 항상 필수", example = "48000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal settingStopLoss;

	@NotNull(message = "설정 익절가는 필수입니다.")
	@Schema(description = "설정 익절가 - 항상 필수", example = "60000.00", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal settingTakeProfit;

	@NotBlank(message = "포지션 진입 근거는 필수입니다.")
	@Schema(description = "포지션 진입 근거 - 항상 필수", requiredMode = Schema.RequiredMode.REQUIRED)
	private String positionStartReason;

	@NotBlank(message = "포지션 탈출 근거는 필수입니다.")
	@Schema(description = "포지션 탈출 근거 - 항상 필수", requiredMode = Schema.RequiredMode.REQUIRED)
	private String positionEndReason;
}