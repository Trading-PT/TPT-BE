package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BillingKeyDirectRegisterResponse {

	/**
	 * 결과 코드
	 * F100: 정상
	 * 기타: 에러
	 */
	private String ResultCode;

	/**
	 * 결과 메시지
	 */
	private String ResultMsg;

	/**
	 * 빌링키 (발급된 빌키, 향후 결제에 사용)
	 * 30 bytes
	 */
	private String BID;

	/**
	 * 거래 ID
	 * 30 bytes
	 */
	private String TID;

	/**
	 * 빌키 발급 일시 (YYMMDDHHMISS 형식에서 변환)
	 * 예: "250114123045" → 2025-01-14T12:30:45
	 * 12 bytes
	 */
	private LocalDateTime AuthDate;

	/**
	 * 카드사 코드
	 * 2 bytes
	 */
	private String CardCode;

	/**
	 * 카드사 명
	 * 20 bytes
	 */
	private String CardName;

	/**
	 * 카드타입 (0: 신용카드, 1: 체크카드)
	 * 1 byte
	 */
	private String CardCl;

	/**
	 * 매입 카드사 코드
	 */
	private String AcquCardCode;

	/**
	 * 매입 카드사명
	 */
	private String AcquCardName;

	/**
	 * 카드번호 (마스킹 처리됨, 예: 123456******1234)
	 * 20 bytes
	 */
	private String CardNo;

	/**
	 * 응답이 성공인지 확인
	 *
	 * @return F100이면 true
	 */
	public boolean isSuccess() {
		return "F100".equals(ResultCode);
	}
}
