package com.tradingpt.tpt_api.domain.subscription.dto.response;

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
 * 구독 고객 목록 슬라이스 응답 래퍼
 * 무한 스크롤 방식의 페이징 정보와 함께 반환
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "구독 고객 목록 슬라이스 응답")
public class SubscriptionCustomerSliceResponseDTO {

    @Schema(description = "구독 고객 목록")
    private List<SubscriptionCustomerResponseDTO> content;

    @Schema(description = "슬라이스 정보")
    private SliceInfo sliceInfo;

    /**
     * Slice를 DTO로 변환하는 팩토리 메서드
     */
    public static SubscriptionCustomerSliceResponseDTO from(Slice<SubscriptionCustomerResponseDTO> slice) {
        return SubscriptionCustomerSliceResponseDTO.builder()
            .content(slice.getContent())
            .sliceInfo(SliceInfo.of(slice))
            .build();
    }
}
