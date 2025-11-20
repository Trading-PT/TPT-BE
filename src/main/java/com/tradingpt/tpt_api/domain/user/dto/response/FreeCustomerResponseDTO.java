package com.tradingpt.tpt_api.domain.user.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 미구독(무료) 고객 조회 응답 DTO
 * ACTIVE 상태의 구독이 없고 담당 트레이너가 없는 고객 정보
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "미구독 고객 응답 DTO")
public class FreeCustomerResponseDTO {

	@Schema(description = "고객 ID", example = "123")
	private Long customerId;

	@Schema(description = "고객 이름", example = "홍길동")
	private String name;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "현재 투자 유형", example = "DAY")
	private InvestmentType primaryInvestmentType;

	@Schema(description = "보유 토큰 수", example = "150")
	private Integer token;

	@Schema(description = "가입일시", example = "2025-01-15T09:00:00")
	private LocalDateTime createdAt;

	/**
	 * Entity를 DTO로 변환하는 정적 팩토리 메서드
	 *
	 * @param customer Customer 엔티티
	 * @return FreeCustomerResponseDTO
	 */
	public static FreeCustomerResponseDTO from(Customer customer) {
		return FreeCustomerResponseDTO.builder()
			.customerId(customer.getId())
			.name(customer.getName())
			.phoneNumber(customer.getPhoneNumber())
			.primaryInvestmentType(customer.getPrimaryInvestmentType())
			.token(customer.getToken())
			.createdAt(customer.getCreatedAt())
			.build();
	}
}
