package com.tradingpt.tpt_api.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MeResponse {
    String name; //유저 이름
    private String username; // 아이디
    private String investmentType;
    boolean isCourseCompleted;
    boolean isPremium;
}
