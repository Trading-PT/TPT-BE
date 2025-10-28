package com.tradingpt.tpt_api.domain.user.dto.response;

import java.util.List;

import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

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
@Schema(description = "내 담당 고객 목록 응답 DTO")
public class MyCustomerListResponseDTO {

	@Schema(description = "담당 고객 목록")
	private List<MyCustomerListItemDTO> customers;

	@Schema(description = "슬라이스 정보")
	private SliceInfo sliceInfo;

	public static MyCustomerListResponseDTO of(
		List<MyCustomerListItemDTO> customers,
		SliceInfo sliceInfo
	) {
		return MyCustomerListResponseDTO.builder()
			.customers(customers)
			.sliceInfo(sliceInfo)
			.build();
	}
}