package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토큰 사용 피드백 리스트 아이템 DTO
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "토큰 사용 피드백 요청 목록 아이템")
public class TokenUsedFeedbackListItemDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String customerName;

	@Schema(description = "투자 타입", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "코스 상태", example = "BEFORE_COMPLETION")
	private CourseStatus courseStatus;

	@Schema(description = "피드백 상태", example = "N")
	private Status status;

	@Schema(description = "작성 시간")
	private LocalDateTime createdAt;

	@Schema(description = "사용한 토큰 개수", example = "1")
	private Integer tokenAmount;

	@Schema(description = "응답한 트레이너 ID (응답 전이면 null)")
	private Long respondedTrainerId;

	@Schema(description = "응답한 트레이너 이름 (응답 전이면 null)")
	private String respondedTrainerName;

	@Schema(description = "피드백 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 일")
	private Integer feedbackDay;

	/**
	 * FeedbackRequest 엔티티에서 DTO 생성
	 */
	public static TokenUsedFeedbackListItemDTO from(FeedbackRequest request) {
		Long trainerId = null;
		String trainerName = null;

		// 응답이 있으면 트레이너 정보 추가
		if (request.getFeedbackResponse() != null) {
			trainerId = request.getFeedbackResponse().getTrainer().getId();
			trainerName = request.getFeedbackResponse().getTrainer().getUsername();
		}

		return TokenUsedFeedbackListItemDTO.builder()
			.id(request.getId())
			.customerId(request.getCustomer().getId())
			.customerName(request.getCustomer().getUsername())
			.investmentType(request.getInvestmentType())
			.courseStatus(request.getCourseStatus())
			.status(request.getStatus())
			.createdAt(request.getCreatedAt())
			.tokenAmount(request.getTokenAmount())
			.respondedTrainerId(trainerId)
			.respondedTrainerName(trainerName)
			.feedbackYear(request.getFeedbackYear())
			.feedbackMonth(request.getFeedbackMonth())
			.feedbackDay(request.getFeedbackRequestDate().getDayOfMonth())
			.build();
	}
}