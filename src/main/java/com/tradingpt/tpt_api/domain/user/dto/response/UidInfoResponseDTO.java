package com.tradingpt.tpt_api.domain.user.dto.response;

import com.tradingpt.tpt_api.domain.user.entity.Uid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "고객이 등록한 UID 한 건")
public class UidInfoResponseDTO {

    @Schema(description = "거래소명 (exchange_name)")
    private String exchangeName;

    @Schema(description = "UID 값")
    private String uid;

    public static UidInfoResponseDTO from(Uid entity) {
        if (entity == null) return null;
        return UidInfoResponseDTO.builder()
                .exchangeName(entity.getExchangeName())
                .uid(entity.getUid())
                .build();
    }
}
