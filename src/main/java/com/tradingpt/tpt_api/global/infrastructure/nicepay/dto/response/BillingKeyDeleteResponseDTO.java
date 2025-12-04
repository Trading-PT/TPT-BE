package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NicePay 빌키 삭제 API 응답 DTO
 *
 * 성공 응답 예시:
 * ResultCode=F101
 * ResultMsg=정상처리
 */
@Getter
@Setter
@NoArgsConstructor
public class BillingKeyDeleteResponseDTO {

    /**
     * 결과 코드
     * F101: 성공 (빌키 삭제 API 전용)
     * 기타: 실패
     */
    private String ResultCode;

    /**
     * 결과 메시지
     */
    private String ResultMsg;

    /**
     * 응답이 성공인지 확인
     *
     * @return F101이면 true (빌키 삭제 성공)
     */
    public boolean isSuccess() {
        return "F101".equals(ResultCode);
    }
}
