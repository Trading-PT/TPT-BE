package com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "투자유형 이력 응답 DTO")
public class InvestmentTypeHistoryResponseDTO {

	@Schema(description = "이력 ID", example = "1")
	private Long id;

	@Schema(description = "투자 유형", example = "SCALPING")
	private InvestmentType investmentType;

	@Schema(description = "투자 유형 설명", example = "스켈핑")
	private String investmentTypeDescription;

	@Schema(description = "시작일", example = "2025-01-01")
	private LocalDate startDate;

	@Schema(description = "종료일 (null이면 현재 진행 중)", example = "2025-06-30")
	private LocalDate endDate;

	@Schema(description = "현재 진행 중 여부", example = "true")
	private boolean isOngoing;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	/**
	 * InvestmentTypeHistory 엔티티를 InvestmentTypeHistoryResponseDTO로 변환
	 *
	 * @param history 투자유형 이력 엔티티
	 * @return InvestmentTypeHistoryResponseDTO
	 */
	public static InvestmentTypeHistoryResponseDTO from(InvestmentTypeHistory history) {
		return InvestmentTypeHistoryResponseDTO.builder()
			.id(history.getId())
			.investmentType(history.getInvestmentType())
			.investmentTypeDescription(history.getInvestmentType().getDescription())
			.startDate(history.getStartDate())
			.endDate(history.getEndDate())
			.isOngoing(history.isOngoing())
			.createdAt(history.getCreatedAt())
			.updatedAt(history.getUpdatedAt())
			.build();
	}
}
