package com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request;

import lombok.Builder;
import lombok.Getter;

/**
 * 나이스페이 비인증 빌키 발급 요청 DTO
 * 카드 정보를 직접 전달하여 빌키를 발급받습니다.
 */
@Getter
@Builder
public class BillingKeyDirectRequestDTO {

	/**
	 * 가맹점 ID
	 */
	private String MID;

	/**
	 * 전문 생성 일시 (yyyyMMddHHmmss)
	 */
	private String EdiDate;

	/**
	 * 가맹점 주문번호
	 */
	private String Moid;

	/**
	 * 암호화된 카드 정보 (AES-128/ECB/PKCS5Padding + Hex)
	 * Plain Text: CardNo={카드번호}&ExpYear={년도}&ExpMonth={월}&IDNo={생년월일}&CardPw={비밀번호}
	 */
	private String EncData;

	/**
	 * 위변조 검증 데이터 (SHA-256 Hex)
	 * SHA-256(MID + EdiDate + Moid + MerchantKey)
	 */
	private String SignData;

	/**
	 * 구매자 이메일 (선택)
	 */
	private String BuyerEmail;

	/**
	 * 구매자 연락처 (선택)
	 */
	private String BuyerTel;

	/**
	 * 구매자명 (선택)
	 */
	private String BuyerName;
}
