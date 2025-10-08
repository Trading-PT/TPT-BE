package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

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
@Schema(description = "방향성에 대한 통계")
public class DirectionStatisticsResponseDTO {

	@Schema(description = "방향성 O")
	private DirectionDetail o;

	@Schema(description = "방향성 x")
	private DirectionDetail x;

	@Getter
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class DirectionDetail {
		private Integer count;
		private Integer winRate;
		private Double rnr;

		public static DirectionDetail of(Integer count, Integer winRate, Double rnr) {
			return DirectionDetail.builder()
				.count(count)
				.winRate(winRate)
				.rnr(rnr)
				.build();
		}
	}

}
