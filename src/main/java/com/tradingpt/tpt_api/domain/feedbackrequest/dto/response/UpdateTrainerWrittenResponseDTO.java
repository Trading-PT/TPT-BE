package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.util.List;

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
@Schema(description = "트레이너 작성 피드백 일괄 업데이트 응답 DTO")
public class UpdateTrainerWrittenResponseDTO {

	@Schema(description = "업데이트된 피드백 요청 수", example = "5")
	private Integer updatedCount;

	@Schema(description = "업데이트된 피드백 요청 ID 목록", example = "[1, 2, 3, 5, 8]")
	private List<Long> updatedIds;

	/**
	 * 업데이트된 ID 목록으로 응답 DTO 생성
	 *
	 * @param updatedIds 업데이트된 피드백 ID 목록
	 * @return UpdateTrainerWrittenResponseDTO
	 */
	public static UpdateTrainerWrittenResponseDTO from(List<Long> updatedIds) {
		return UpdateTrainerWrittenResponseDTO.builder()
			.updatedCount(updatedIds.size())
			.updatedIds(updatedIds)
			.build();
	}
}
