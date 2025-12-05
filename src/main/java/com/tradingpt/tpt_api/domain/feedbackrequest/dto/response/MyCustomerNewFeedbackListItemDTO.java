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
 * 내 담당 고객의 새로운 피드백 요청 리스트 아이템 DTO
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "내 담당 고객의 새로운 피드백 요청 아이템")
public class MyCustomerNewFeedbackListItemDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String customerName;

	@Schema(description = "uid")
	private String uid;

	@Schema(description = "피드백 제목")
	private String title;

	@Schema(description = "투자 타입", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "코스 상태", example = "BEFORE_COMPLETION")
	private CourseStatus courseStatus;

	@Schema(description = "피드백 상태", example = "N")
	private Status status;

	@Schema(description = "작성 시간")
	private LocalDateTime createdAt;

	@Schema(description = "피드백 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 일")
	private Integer feedbackDay;

	@Schema(description = "토큰 사용 여부")
	private Boolean isTokenUsed;

	/**
	 * FeedbackRequest 엔티티에서 DTO 생성
	 */
	public static MyCustomerNewFeedbackListItemDTO from(FeedbackRequest request) {
		return MyCustomerNewFeedbackListItemDTO.builder()
			.id(request.getId())
			.customerId(request.getCustomer().getId())
			.customerName(request.getCustomer().getUsername())
			.uid(request.getCustomer().getUid().getUid())
			.title(request.getTitle())
			.investmentType(request.getInvestmentType())
			.courseStatus(request.getCourseStatus())
			.status(request.getStatus())
			.createdAt(request.getCreatedAt())
			.feedbackYear(request.getFeedbackYear())
			.feedbackMonth(request.getFeedbackMonth())
			.feedbackDay(request.getFeedbackRequestDate().getDayOfMonth())
			.isTokenUsed(request.getIsTokenUsed())
			.build();
	}

}
