package com.tradingpt.tpt_api.global.infrastructure.nicepay.service;

import org.springframework.stereotype.Service;

import com.tradingpt.tpt_api.global.infrastructure.nicepay.client.NicePayFeignClient;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.config.NicePayConfig;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDeleteRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDirectRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyRegisterRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyDeleteResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyDirectRegisterResponse;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyRegisterResponse;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayErrorStatus;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayException;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.util.NicePayCryptoUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NicePay API 서비스 래퍼
 * Feign Client를 사용하여 NicePay API를 호출하고 응답을 검증합니다.
 * 인증/비인증 빌키 발급 방식 모두 지원
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NicePayService {

	private final NicePayFeignClient nicePayFeignClient;
	private final NicePayConfig nicePayConfig;

	/**
	 * 빌키 발급 API 호출 (인증 방식)
	 *
	 * @param txTid NicePay 인증 응답의 TxTid
	 * @param authToken NicePay 인증 응답의 AuthToken
	 * @param moid 서버에서 생성한 주문번호
	 * @return 빌키 발급 응답 (BID 포함)
	 * @throws NicePayException 빌키 발급 실패 시
	 */
	public BillingKeyRegisterResponse registerBillingKey(
		String txTid,
		String authToken,
		String moid
	) {
		log.info("빌키 발급 요청: txTid={}, moid={}", txTid, moid);

		String mid = nicePayConfig.getMid();
		String merchantKey = nicePayConfig.getMerchantKey();

		// EdiDate 생성
		String ediDate = NicePayCryptoUtil.generateEdiDate();

		// SignData 생성: SHA256(TID + MID + EdiDate + MerchantKey)
		String signData = NicePayCryptoUtil.generateSignData(
			txTid,
			mid,
			ediDate,
			merchantKey
		);

		// 요청 객체 생성
		BillingKeyRegisterRequestDTO request = BillingKeyRegisterRequestDTO.builder()
			.TID(txTid)
			.MID(mid)
			.AuthToken(authToken)
			.EdiDate(ediDate)
			.SignData(signData)
			.build();

		try {
			// Feign Client로 API 호출
			BillingKeyRegisterResponse response = nicePayFeignClient.registerBillingKey(request);

			// 응답 검증
			if (response.isSuccess()) {
				log.info("빌키 발급 성공: BID={}, CardName={}, CardNo={}",
					response.getBID(), response.getCardName(), response.getCardNo());
				return response;
			} else {
				log.error("빌키 발급 실패: ResultCode={}, ResultMsg={}",
					response.getResultCode(), response.getResultMsg());
				NicePayErrorStatus errorStatus = NicePayErrorStatus.fromResultCode(response.getResultCode());
				throw new NicePayException(errorStatus);
			}
		} catch (NicePayException e) {
			throw e;
		} catch (Exception e) {
			log.error("빌키 발급 API 호출 중 오류 발생", e);
			throw new NicePayException(NicePayErrorStatus.API_CONNECTION_FAILED);
		}
	}

	/**
	 * 빌키 발급 API 호출 (비인증 방식)
	 * 카드 정보를 직접 전달하여 빌키를 발급받습니다.
	 *
	 * @param cardInfoPlainText 카드 정보 평문 (CardNo={카드번호}&ExpYear={년도}&ExpMonth={월}&IDNo={생년월일}&CardPw={비밀번호})
	 * @param moid 서버에서 생성한 주문번호
	 * @param buyerEmail 구매자 이메일 (선택)
	 * @param buyerTel 구매자 연락처 (선택)
	 * @param buyerName 구매자명 (선택)
	 * @return 빌키 발급 응답 (BID 포함)
	 * @throws NicePayException 빌키 발급 실패 시
	 */
	public BillingKeyDirectRegisterResponse registerBillingKeyDirect(
		String cardInfoPlainText,
		String moid,
		String buyerEmail,
		String buyerTel,
		String buyerName
	) {
		log.info("비인증 빌키 발급 요청: moid={}", moid);

		String mid = nicePayConfig.getMid();
		String merchantKey = nicePayConfig.getMerchantKey();

		// EdiDate 생성
		String ediDate = NicePayCryptoUtil.generateEdiDate();

		// EncData 생성: AES-128/ECB/PKCS5Padding 암호화 + Hex 인코딩
		String encData = NicePayCryptoUtil.encryptAES(cardInfoPlainText, merchantKey);

		// SignData 생성: SHA256(MID + EdiDate + Moid + MerchantKey)
		String signData = NicePayCryptoUtil.generateSignData(
			mid,
			ediDate,
			moid,
			merchantKey
		);

		// 요청 객체 생성
		BillingKeyDirectRequestDTO request = BillingKeyDirectRequestDTO.builder()
			.MID(mid)
			.EdiDate(ediDate)
			.Moid(moid)
			.EncData(encData)
			.SignData(signData)
			.BuyerEmail(buyerEmail)
			.BuyerTel(buyerTel)
			.BuyerName(buyerName)
			.build();

		try {
			// Feign Client로 API 호출
			BillingKeyDirectRegisterResponse response = nicePayFeignClient.registerBillingKeyDirect(request);

			// 응답 검증
			if (response.isSuccess()) {
				log.info("비인증 빌키 발급 성공: BID={}, CardName={}, CardNo={}",
					response.getBID(), response.getCardName(), response.getCardNo());
				return response;
			} else {
				log.error("비인증 빌키 발급 실패: ResultCode={}, ResultMsg={}",
					response.getResultCode(), response.getResultMsg());
				NicePayErrorStatus errorStatus = NicePayErrorStatus.fromResultCode(response.getResultCode());
				throw new NicePayException(errorStatus);
			}
		} catch (NicePayException e) {
			throw e;
		} catch (Exception e) {
			log.error("비인증 빌키 발급 API 호출 중 오류 발생", e);
			throw new NicePayException(NicePayErrorStatus.API_CONNECTION_FAILED);
		}
	}

	/**
	 * 빌키 삭제 API 호출
	 *
	 * @param billingKey 삭제할 빌링키 (BID)
	 * @return 빌키 삭제 응답
	 * @throws NicePayException 빌키 삭제 실패 시
	 */
	public BillingKeyDeleteResponseDTO deleteBillingKey(String billingKey) {
		log.info("빌키 삭제 요청: BID={}", billingKey);

		String mid = nicePayConfig.getMid();
		String merchantKey = nicePayConfig.getMerchantKey();

		// EdiDate 생성
		String ediDate = NicePayCryptoUtil.generateEdiDate();

		// SignData 생성: SHA256(MID + EdiDate + BID + MerchantKey)
		String signData = NicePayCryptoUtil.generateSignData(
			mid,
			ediDate,
			billingKey,
			merchantKey
		);

		// 요청 객체 생성
		BillingKeyDeleteRequestDTO request = BillingKeyDeleteRequestDTO.builder()
			.MID(mid)
			.BID(billingKey)
			.EdiDate(ediDate)
			.SignData(signData)
			.build();

		try {
			// Feign Client로 API 호출
			BillingKeyDeleteResponseDTO response = nicePayFeignClient.deleteBillingKey(request);

			// 응답 검증
			if (response.isSuccess()) {
				log.info("빌키 삭제 성공: BID={}", billingKey);
				return response;
			} else {
				log.error("빌키 삭제 실패: ResultCode={}, ResultMsg={}",
					response.getResultCode(), response.getResultMsg());
				NicePayErrorStatus errorStatus = NicePayErrorStatus.fromResultCode(response.getResultCode());
				throw new NicePayException(errorStatus);
			}
		} catch (NicePayException e) {
			throw e;
		} catch (Exception e) {
			log.error("빌키 삭제 API 호출 중 오류 발생", e);
			throw new NicePayException(NicePayErrorStatus.API_CONNECTION_FAILED);
		}
	}
}
