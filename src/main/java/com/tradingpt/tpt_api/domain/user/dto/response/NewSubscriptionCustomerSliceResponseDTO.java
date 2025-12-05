package com.tradingpt.tpt_api.domain.user.dto.response;

import java.util.List;

import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 신규 구독 고객 목록 슬라이스 응답 래퍼
 * 무한 스크롤 방식의 페이징 정보와 총 인원 수를 함께 반환
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "신규 구독 고객 목록 슬라이스 응답")
public class NewSubscriptionCustomerSliceResponseDTO {

	@Schema(description = "신규 구독 고객 총 인원 수", example = "25")
	private Long totalCount;

	@Schema(description = "신규 구독 고객 목록")
	private List<NewSubscriptionCustomerResponseDTO> content;

	@Schema(description = "슬라이스 정보")
	private SliceInfo sliceInfo;

	/**
	 * Slice와 총 인원 수를 DTO로 변환하는 팩토리 메서드
	 *
	 * @param slice 신규 구독 고객 Slice
	 * @param totalCount 신규 구독 고객 총 인원 수
	 * @return NewSubscriptionCustomerSliceResponseDTO
	 */
	public static NewSubscriptionCustomerSliceResponseDTO from(
		Slice<NewSubscriptionCustomerResponseDTO> slice,
		Long totalCount
	) {
		return NewSubscriptionCustomerSliceResponseDTO.builder()
			.totalCount(totalCount)
			.content(slice.getContent())
			.sliceInfo(SliceInfo.of(slice))
			.build();
	}
}
