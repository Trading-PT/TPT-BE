package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

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
@Schema(description = "특정 날짜의 트레이딩 피드백 요청 목록 응답 DTO")
public class DailyFeedbackRequestsResponseDTO {

	@Schema(description = "피드백 요청 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 요청 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 요청 주차")
	private Integer feedbackWeek;

	@Schema(description = "피드백 요청 일자")
	private LocalDate feedbackDate;

	@Schema(description = "해당 날짜의 피드백 요청 개수")
	private Integer totalCount;

	@Schema(description = "해당 날짜의 피드백 요청 리스트")
	private List<DailyFeedbackRequestSummaryDTO> feedbackRequests;

	// 팩토리 메서드
	public static DailyFeedbackRequestsResponseDTO of(
		LocalDate feedbackDate,
		Integer feedbackYear,
		Integer feedbackMonth,
		Integer feedbackWeek,
		List<DailyFeedbackRequestSummaryDTO> feedbackRequests
	) {
		return DailyFeedbackRequestsResponseDTO.builder()
			.feedbackDate(feedbackDate)
			.feedbackYear(feedbackYear)
			.feedbackMonth(feedbackMonth)
			.feedbackWeek(feedbackWeek)
			.totalCount(feedbackRequests.size())
			.feedbackRequests(feedbackRequests)
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Schema(description = "특정 날짜의 트레이딩 피드백 요약 정보 DTO")
	public static class DailyFeedbackRequestSummaryDTO {

		@Schema(description = "피드백 요청 ID")
		private Long feedbackRequestId;

		@Schema(description = "일자 내 생성 순번 (1부터 시작)")
		private Integer dailySequence;

		@Schema(description = "피드백 제목")
		private String title;

		@Schema(description = "피드백 타입")
		private FeedbackType feedbackType;

		@Schema(description = "피드백 상태")
		private Status status;

		@Schema(description = "피드백 생성 일시")
		private LocalDateTime createdAt;

		@Schema(description = "트레이너 답변 여부")
		private Boolean isResponded;

		@Schema(description = "트레이너 답변 읽음 여부")
		private Boolean isRead;

		@Schema(description = "Trainer 답변 대기 여부 (UI Label 용)")
		private Boolean isWaitingForTrainerResponse;

		@Schema(description = "Trainer 답변 미확인 여부 (빨간 점 표시)")
		private Boolean hasUnreadTrainerResponse;

		public static DailyFeedbackRequestSummaryDTO of(FeedbackRequest feedbackRequest, Integer dailySequence) {
			Status status = feedbackRequest.getStatus();
			boolean responded = Boolean.TRUE.equals(feedbackRequest.getIsResponded());
			boolean read = Boolean.TRUE.equals(feedbackRequest.getIsRead());

			return DailyFeedbackRequestSummaryDTO.builder()
				.feedbackRequestId(feedbackRequest.getId())
				.dailySequence(dailySequence)
				.title(feedbackRequest.getTitle())
				.feedbackType(feedbackRequest.getFeedbackType())
				.status(status)
				.createdAt(feedbackRequest.getCreatedAt())
				.isResponded(responded)
				.isRead(read)
				.isWaitingForTrainerResponse(status == Status.N)
				.hasUnreadTrainerResponse(status == Status.FN)
				.build();
		}
	}
}
