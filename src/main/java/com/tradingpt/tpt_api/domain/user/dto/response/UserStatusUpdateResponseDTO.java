package com.tradingpt.tpt_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "사용자 상태 변경 응답 DTO")
public class UserStatusUpdateResponseDTO {

    @Schema(description = "상태 변경된 사용자 ID", example = "123")
    private Long userId;

    public static UserStatusUpdateResponseDTO of(Long id) {
        return UserStatusUpdateResponseDTO.builder()
                .userId(id)
                .build();
    }
}
