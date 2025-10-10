package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

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
@Schema(description = "어드민용 트레이딩 피드백 요청 목록 응답 DTO (무한 스크롤)")
public class AdminFeedbackResponseDTO {

	@Schema(description = "현재 선정된 베스트 피드백")
	private SelectedBestFeedbackListResponseDTO selectedBestFeedbackListResponseDTO;

	@Schema(description = "전체 피드백 목록")
	private TotalFeedbackListResponseDTO totalFeedbackListResponseDTO;

	public static AdminFeedbackResponseDTO of(
		SelectedBestFeedbackListResponseDTO selectedBestFeedbackListResponseDTO,
		TotalFeedbackListResponseDTO totalFeedbackListResponseDTO) {
		return AdminFeedbackResponseDTO.builder()
			.selectedBestFeedbackListResponseDTO(selectedBestFeedbackListResponseDTO)
			.totalFeedbackListResponseDTO(totalFeedbackListResponseDTO)
			.build();
	}

}
