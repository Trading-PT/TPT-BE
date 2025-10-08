package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "진입 타점에 대한 통계")
public class EntryPointStatisticsResponseDTO {
	@Schema(description = "Reverse (역추세)")
	private PositionDetail reverse;

	@Schema(description = "Pull back (되돌림)")
	private PositionDetail pullBack;

	@Schema(description = "Break out (돌파)")
	private PositionDetail breakOut;

	public static EntryPointStatisticsResponseDTO of(PositionDetail reverse, PositionDetail pullBack,
		PositionDetail breakOut) {
		return EntryPointStatisticsResponseDTO.builder()
			.reverse(reverse)
			.pullBack(pullBack)
			.breakOut(breakOut)
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PositionDetail {
		private Integer count;
		private Double winRate;
		private Double rnr;

		public static PositionDetail of(Integer count, Double winRate, Double rnr) {
			return PositionDetail.builder()
				.count(count)
				.winRate(winRate)
				.rnr(rnr)
				.build();
		}
	}
}
