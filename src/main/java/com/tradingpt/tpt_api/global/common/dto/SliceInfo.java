package com.tradingpt.tpt_api.global.common.dto;

import org.springframework.data.domain.Slice;

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
@Schema(description = "슬라이스 정보 (무한 스크롤용)")
public class SliceInfo {
	@Schema(description = "현재 페이지", example = "0")
	private Integer currentPage;

	@Schema(description = "페이지 크기", example = "12")
	private Integer pageSize;

	@Schema(description = "다음 페이지 존재 여부", example = "true")
	private Boolean hasNext;

	@Schema(description = "첫 페이지 여부", example = "true")
	private Boolean isFirst;

	@Schema(description = "마지막 페이지 여부", example = "false")
	private Boolean isLast;

	public static SliceInfo of(Slice<?> slice) {
		return SliceInfo.builder()
			.currentPage(slice.getNumber())
			.pageSize(slice.getSize())
			.hasNext(slice.hasNext())
			.isFirst(slice.isFirst())
			.isLast(slice.isLast())
			.build();
	}
}