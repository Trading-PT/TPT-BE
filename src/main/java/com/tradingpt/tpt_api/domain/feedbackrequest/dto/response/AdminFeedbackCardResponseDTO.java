package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "어드민용 피드백 카드 DTO (목록용)")
public class AdminFeedbackCardResponseDTO {

	@Schema(description = "피드백 ID")
	private Long id;

	@Schema(description = "베스트 피드백 여부")
	private Boolean isBestFeedback;

	@Schema(description = "트레이너 작성 여부")
	private Boolean isTrainerWritten;

	@Schema(description = "이름")
	private String username;

	@Schema(description = "담당 트레이너 이름")
	private String trainerName;

	@Schema(description = "투자 유형")
	private InvestmentType investmentType;

	@Schema(description = "완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "요청 일자")
	private LocalDateTime createdAt;

	@Schema(description = "제공 일자")
	private LocalDateTime submittedAt;

	public static AdminFeedbackCardResponseDTO of(Long id, Boolean isBestFeedback, Boolean isTrainerWritten,
		String username, String trainerName, InvestmentType investmentType,
		CourseStatus courseStatus, LocalDateTime createdAt, LocalDateTime submittedAt) {
		return AdminFeedbackCardResponseDTO.builder()
			.id(id)
			.isBestFeedback(isBestFeedback)
			.isTrainerWritten(isTrainerWritten)
			.username(username)
			.trainerName(trainerName)
			.investmentType(investmentType)
			.courseStatus(courseStatus)
			.createdAt(createdAt)
			.submittedAt(submittedAt)
			.build();
	}
}
