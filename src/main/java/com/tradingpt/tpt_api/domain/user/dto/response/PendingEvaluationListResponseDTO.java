package com.tradingpt.tpt_api.domain.user.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 미작성 평가 목록 응답 DTO
 * 완강 월부터 현재 월까지의 모든 미작성 평가를 조회
 * 무한 스크롤 방식 (Slice 페이징)
 */
@Getter
@Builder
@Schema(description = "미작성 평가 목록 응답 DTO")
public class PendingEvaluationListResponseDTO {

	@Schema(description = "미작성 평가 목록 (고객별 × 평가 대상별 행 분리)")
	private List<PendingEvaluationItemDTO> evaluations;

	@Schema(description = "슬라이스 정보 (무한 스크롤용)")
	private SliceInfo sliceInfo;

	/**
	 * Slice 결과를 DTO로 변환
	 */
	public static PendingEvaluationListResponseDTO of(
		Slice<PendingEvaluationItemDTO> slice
	) {
		return PendingEvaluationListResponseDTO.builder()
			.evaluations(slice.getContent())
			.sliceInfo(SliceInfo.of(slice))
			.build();
	}
}
