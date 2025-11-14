package com.tradingpt.tpt_api.domain.paymentmethod.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 빌키 등록 초기화 응답 DTO
 * 프론트엔드에서 NicePay 인증창을 띄우는데 필요한 정보를 제공합니다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BillingKeyInitResponseDTO {

	/**
	 * 서버에서 생성한 고유 주문번호 (Moid)
	 * 프론트엔드는 이 값을 NicePay 인증창에 전달하고,
	 * 인증 완료 후 빌키 등록 API 호출 시 다시 서버로 전송해야 합니다.
	 */
	private String moid;

	/**
	 * 전문 생성 일시 (yyyyMMddHHmmss)
	 */
	private String ediDate;

	/**
	 * 위변조 검증 데이터
	 * SHA256(MID + EdiDate + Moid + MerchantKey)
	 */
	private String signData;

	/**
	 * NicePay 설정 정보
	 * 프론트엔드에서 NicePay SDK 호출 시 사용
	 */
	private NicePayConfigDTO nicePayConfig;

	public static BillingKeyInitResponseDTO of(String moid, String ediDate, String signData
		, String mid, String goodName, String amt) {
		return BillingKeyInitResponseDTO.builder()
			.moid(moid)
			.ediDate(ediDate)
			.signData(signData)
			.nicePayConfig(BillingKeyInitResponseDTO.NicePayConfigDTO.of(mid, goodName, amt))
			.build();

	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	public static class NicePayConfigDTO {
		/**
		 * 상점 ID
		 */
		private String mid;

		/**
		 * 상품명
		 */
		private String goodsName;

		/**
		 * 금액 (빌키 발급시 실제 결제되지 않음, 임의의 값)
		 */
		private String amt;

		/**
		 * 결제 수단 (CARD 고정)
		 */
		private String payMethod;

		/**
		 * 빌키 인증 여부 (Y 고정)
		 */
		private String billAuthYn;

		public static NicePayConfigDTO of(String mid, String goodsName, String amt) {
			return BillingKeyInitResponseDTO.NicePayConfigDTO.builder()
				.mid(mid)
				.goodsName(goodsName)
				.amt(amt)
				.payMethod("CARD")
				.billAuthYn("Y")
				.build();
		}
	}
}
