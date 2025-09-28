package com.tradingpt.tpt_api.domain.feedbackrequest.dto.request;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.PreCourseFeedbackDetail;
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
@Schema(description = "스켈핑 피드백 요청 DTO")
public class CreateScalpingRequestDetailRequestDTO {

	@Schema(description = "완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "멤버쉽")
	private MembershipLevel membershipLevel;

	@Schema(description = "요청 날짜")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate requestDate;

	@Schema(description = "포지션 홀딩 시간")
	private String positionHoldingTime;

	@Schema(description = "종목")
	private String category;

	@Schema(description = "하루 매매 횟수")
	private Integer dailyTradingCount;

	@Schema(description = "스크린샷 이미지 파일")
	private List<MultipartFile> screenshotFiles;

	@Schema(description = "리스크 테이킹 (1-100)")
	private Integer riskTaking;

	@Schema(description = "레버리지 (1-1000)")
	private Integer leverage;

	@Schema(description = "총 포지션 잡은 횟수")
	private Integer totalPositionTakingCount;

	@Schema(description = "총 매매 횟수 대비 수익 매매횟수")
	private Integer totalProfitMarginPerTrades;

	@Schema(description = "담당 트레이너 피드백 요청 사항")
	private String trainerFeedbackRequestContent;

	@Schema(description = "15분봉 기준 추세 분석")
	private String trendAnalysis;

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
