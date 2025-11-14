package com.tradingpt.tpt_api.global.infrastructure.nicepay.service;

import org.springframework.stereotype.Service;

import com.tradingpt.tpt_api.global.infrastructure.nicepay.client.NicePayFeignClient;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.config.NicePayConfig;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDeleteRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyRegisterRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyDeleteResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyRegisterResponse;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayErrorStatus;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.exception.NicePayException;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.util.NicePayCryptoUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * NicePay API 서비스 래퍼
 * Feign Client를 사용하여 NicePay API를 호출하고 응답을 검증합니다.
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
