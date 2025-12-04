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
 * 미구독(무료) 고객 목록 슬라이스 응답 래퍼
 * 무한 스크롤 방식의 페이징 정보와 총 인원 수를 함께 반환
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "미구독 고객 목록 슬라이스 응답")
public class FreeCustomerSliceResponseDTO {

    @Schema(description = "미구독 고객 총 인원 수", example = "85")
    private Long totalCount;

    @Schema(description = "미구독 고객 목록")
    private List<FreeCustomerResponseDTO> content;

    @Schema(description = "슬라이스 정보")
    private SliceInfo sliceInfo;

    /**
     * Slice와 총 인원 수를 DTO로 변환하는 팩토리 메서드
     *
     * @param slice 미구독 고객 Slice
     * @param totalCount 미구독 고객 총 인원 수
     * @return FreeCustomerSliceResponseDTO
     */
    public static FreeCustomerSliceResponseDTO from(
        Slice<FreeCustomerResponseDTO> slice,
        Long totalCount
    ) {
        return FreeCustomerSliceResponseDTO.builder()
            .totalCount(totalCount)
            .content(slice.getContent())
            .sliceInfo(SliceInfo.of(slice))
            .build();
    }
}
