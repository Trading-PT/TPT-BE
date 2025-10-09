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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
	@Min(value = 1, message = "리스크 테이킹은 1 이상이어야 합니다.")
	@Max(value = 100, message = "리스크 테이킹은 100 이하여야 합니다.")
	@Schema(description = "리스크 테이킹 (1-100)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer riskTaking;

	@NotNull(message = "레버리지는 필수입니다.")
	@Min(value = 1, message = "레버리지는 1 이상이어야 합니다.")
	@Max(value = 1000, message = "레버리지는 1000 이하여야 합니다.")
	@Schema(description = "레버리지 (1-1000)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Integer leverage;

	@NotNull(message = "포지션은 필수입니다.")
	@Schema(description = "포지션 (LONG/SHORT)", requiredMode = Schema.RequiredMode.REQUIRED)
	private Position position;

	@NotNull(message = "P&L은 필수입니다.")
	@Schema(description = "P&L", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal pnl;

	@NotNull(message = "손익비는 필수입니다.")
	@Schema(description = "손익비", requiredMode = Schema.RequiredMode.REQUIRED)
	private Double rnr;
	@Schema(description = "매매 복기")
	private String tradingReview;

	// ========================================
	// 완강 전 전용 필드
	// ========================================

	@Schema(description = "비중 (운용 자금 대비) - 완강 전 필수", example = "50")
	private Integer operatingFundsRatio;

	@Schema(description = "진입 가격 - 완강 전 필수", example = "50000.00")
	private BigDecimal entryPrice;

	@Schema(description = "탈출 가격 - 완강 전 필수", example = "55000.00")
	private BigDecimal exitPrice;

	@Schema(description = "설정 손절가 - 완강 전 필수", example = "48000.00")
	private BigDecimal settingStopLoss;

	@Schema(description = "설정 익절가 - 완강 전 필수", example = "60000.00")
	private BigDecimal settingTakeProfit;

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

	@Schema(description = "1차 진입 타점")
	private EntryPoint entryPoint1;

	@Schema(description = "등급")
	private Grade grade;

	@Schema(description = "2차 진입 타점")
	private String entryPoint2;

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
			return operatingFundsRatio != null
				&& entryPrice != null
				&& exitPrice != null
				&& settingStopLoss != null
				&& settingTakeProfit != null
				&& positionStartReason != null && !positionStartReason.isBlank()
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
				&& entryPoint1 != null
				&& entryPoint2 != null;
		}
		return true;
	}

}
