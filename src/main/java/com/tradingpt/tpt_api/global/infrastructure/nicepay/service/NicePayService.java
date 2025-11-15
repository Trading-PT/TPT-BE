package com.tradingpt.tpt_api.global.infrastructure.nicepay.service;

import org.springframework.stereotype.Service;

import com.tradingpt.tpt_api.global.infrastructure.nicepay.client.NicePayFeignClient;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.config.NicePayConfig;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDeleteRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDirectRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyRegisterRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.RecurringPaymentRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyDeleteResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyRegisterResponse;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.RecurringPaymentResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayErrorStatus;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayException;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.util.NicePayCryptoUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NicePay API 서비스 래퍼
 * Feign Client를 사용하여 NicePay API를 호출하고 응답을 검증합니다.
 * 인증/비인증 빌키 발급, 빌링 결제, 빌키 삭제 기능 제공
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
	public BillingKeyRegisterResponse registerBillingKeyDirect(
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
			BillingKeyRegisterResponse response = nicePayFeignClient.registerBillingKeyDirect(request);

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
	public BillingKeyDeleteResponseDTO deleteBillingKey(String moid, String billingKey) {
		log.info("빌키 삭제 요청: BID={}", billingKey);

		String mid = nicePayConfig.getMid();
		String merchantKey = nicePayConfig.getMerchantKey();

		// EdiDate 생성
		String ediDate = NicePayCryptoUtil.generateEdiDate();

		// SignData 생성: SHA256(MID + EdiDate + Moid + BID + MerchantKey)
		String signData = NicePayCryptoUtil.generateSignData(
			mid,
			ediDate,
			moid,
			billingKey,
			merchantKey
		);

		// 요청 객체 생성
		BillingKeyDeleteRequestDTO request = BillingKeyDeleteRequestDTO.builder()
			.MID(mid)
			.BID(billingKey)
			.EdiDate(ediDate)
			.Moid(moid)
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

	/**
	 * 빌링 결제(승인) API 호출
	 * 발급받은 빌링키(BID)를 사용하여 정기 결제를 실행합니다.
	 *
	 * @param billingKey 빌링키 (BID)
	 * @param amount 결제 금액
	 * @param orderName 주문명
	 * @param moid 주문번호
	 * @return 빌링 결제 응답 (승인번호, 거래번호 등)
	 * @throws NicePayException 빌링 결제 실패 시
	 */
	public RecurringPaymentResponseDTO executeRecurringPayment(
		String billingKey,
		String amount,
		String orderName,
		String moid
	) {
		log.info("빌링 결제 요청: BID={}, Amount={}, Moid={}", billingKey, amount, moid);

		String mid = nicePayConfig.getMid();
		String merchantKey = nicePayConfig.getMerchantKey();

		// EdiDate 생성
		String ediDate = NicePayCryptoUtil.generateEdiDate();

		// TID 생성 (반드시 새로 생성)
		String tid = NicePayCryptoUtil.generateTID(mid);

		// SignData 생성: SHA256(MID + EdiDate + Moid + Amt + BID + MerchantKey)
		String signData = NicePayCryptoUtil.generateSignData(
			mid,
			ediDate,
			moid,
			amount,
			billingKey,
			merchantKey
		);

		// 요청 객체 생성
		RecurringPaymentRequestDTO request = RecurringPaymentRequestDTO.builder()
			.BID(billingKey)
			.MID(mid)
			.TID(tid)
			.EdiDate(ediDate)
			.Moid(moid)
			.Amt(amount)
			.GoodsName(orderName)
			.SignData(signData)
			.CardInterest("0")  // 일반 결제
			.CardQuota("00")     // 일시불
			.build();

		try {
			// Feign Client로 API 호출
			RecurringPaymentResponseDTO response = nicePayFeignClient.executeRecurringPayment(request);

			// 응답 검증
			if (response.isSuccess()) {
				log.info("빌링 결제 성공: TID={}, AuthCode={}, Amt={}",
					response.getTID(), response.getAuthCode(), response.getAmt());
				return response;
			} else {
				log.error("빌링 결제 실패: ResultCode={}, ResultMsg={}",
					response.getResultCode(), response.getResultMsg());
				NicePayErrorStatus errorStatus = NicePayErrorStatus.fromResultCode(response.getResultCode());
				throw new NicePayException(errorStatus);
			}
		} catch (NicePayException e) {
			throw e;
		} catch (Exception e) {
			log.error("빌링 결제 API 호출 중 오류 발생", e);
			throw new NicePayException(NicePayErrorStatus.API_CONNECTION_FAILED);
		}
	}
}
