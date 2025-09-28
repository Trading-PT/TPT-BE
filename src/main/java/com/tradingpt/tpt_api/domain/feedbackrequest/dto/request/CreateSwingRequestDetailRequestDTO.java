package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.PreCourseFeedbackDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "스윙 피드백 요청 DTO")
public class CreateSwingRequestDetailRequestDTO {

	@Schema(description = "완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "멤버쉽")
	private MembershipLevel membershipLevel;

	@Schema(description = "피드백 요청 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 요청 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 요청 주차")
	private Integer feedbackWeek;

	@Schema(description = "요청 날짜")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate requestDate;

	@Schema(description = "포지션 홀딩 시간")
	private String positionHoldingTime;

	@Schema(description = "종목")
	private String category;

	@Schema(description = "포지션 진입 날짜")
	private LocalDate positionStartDate;

	@Schema(description = "포지션 종료 날짜")
	private LocalDate positionEndDate;

	@Schema(description = "스크린샷 이미지 파일")
	private List<MultipartFile> screenshotFiles;

	@Schema(description = "리스크 테이킹 (1-100)")
	private Integer riskTaking;

	@Schema(description = "레버리지 (1-1000)")
	private Integer leverage;

	@Schema(description = "포지션 (LONG/SHORT)")
	private Position position;

	@Schema(description = "담당 트레이너 피드백 요청 사항")
	private String trainerFeedbackRequestContent;

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

	@Schema(description = "P&L")
	private BigDecimal pnl;

	@Schema(description = "손익비")
	private String winLossRatio;

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

	@Valid
	@Schema(description = "완강 전 고객이 입력하는 상세 정보")
	private PreCourseFeedbackDetailRequestDTO preCourseFeedbackDetail;

	@JsonIgnore
	public PreCourseFeedbackDetail toPreCourseFeedbackDetail() {
		if (courseStatus == null) {
			return null;
		}

		return courseStatus == CourseStatus.BEFORE_COMPLETION && preCourseFeedbackDetail != null
			? preCourseFeedbackDetail.toEntity()
			: null;
	}

	@AssertTrue(message = "완강 전 요청은 preCourseFeedbackDetail 입력이 필요합니다.")
	@JsonIgnore
	public boolean isPreCourseDetailValid() {
		if (courseStatus == CourseStatus.BEFORE_COMPLETION) {
			return preCourseFeedbackDetail != null && !preCourseFeedbackDetail.isEmpty();
		}
		return true;
	}

}
