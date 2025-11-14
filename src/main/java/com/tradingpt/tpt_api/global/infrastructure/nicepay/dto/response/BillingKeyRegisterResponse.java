package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * NicePay 빌키 발급 API 응답 DTO
 *
 * 성공 응답 예시:
 * ResultCode=F100
 * ResultMsg=정상처리
 * BID=BK_nictest04m_202501141234567890
 * TID=nictest04m01012501141234567890
 * AuthDate=20250114
 * CardCode=01
 * CardName=신한카드
 * CardNo=123456******1234
 */
@Getter
@Setter
@NoArgsConstructor
public class BillingKeyRegisterResponse {

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
	 * 카드번호 (마스킹 처리됨, 예: 123456******1234)
	 * 20 bytes
	 */
	private String CardNo;

	/**
	 * 카드 유효기간 (yymm)
	 * 4 bytes (선택)
	 */
	private String ExpYear;
	private String ExpMonth;

	/**
	 * 응답이 성공인지 확인
	 *
	 * @return F100이면 true
	 */
	public boolean isSuccess() {
		return "F100".equals(ResultCode);
	}
}
