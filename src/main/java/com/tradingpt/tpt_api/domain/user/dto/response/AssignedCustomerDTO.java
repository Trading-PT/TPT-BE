package com.tradingpt.tpt_api.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "트레이너에게 배정된 고객 정보 DTO")
public class AssignedCustomerDTO {

    @Schema(description = "고객 ID", example = "101")
    private Long customerId;

    @Schema(description = "고객 이름", example = "홍길동")
    private String name;
}
