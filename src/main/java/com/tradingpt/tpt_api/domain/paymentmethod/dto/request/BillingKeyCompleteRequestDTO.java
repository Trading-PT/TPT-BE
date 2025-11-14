package com.tradingpt.tpt_api.domain.paymentmethod.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 빌키 등록 완료 요청 DTO
 * 프론트엔드에서 NicePay 인증창 응답 데이터를 전달받습니다.
 */
@Getter
@NoArgsConstructor
public class BillingKeyCompleteRequestDTO {

    /**
     * NicePay 인증 응답의 거래 ID (TxTid)
     */
    @NotBlank(message = "거래 ID는 필수입니다.")
    private String txTid;

    /**
     * NicePay 인증 응답의 인증 토큰 (AuthToken)
     */
    @NotBlank(message = "인증 토큰은 필수입니다.")
    private String authToken;

    /**
     * 서버에서 생성한 주문번호 (Moid)
     * 초기화 요청에서 받은 값과 동일해야 함
     */
    @NotBlank(message = "주문번호는 필수입니다.")
    private String moid;
}
