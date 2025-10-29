package com.tradingpt.tpt_api.domain.user.dto.response;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 내 담당 고객 리스트 아이템 DTO
 */
@Getter
@Builder
@Schema(description = "내 담당 고객 정보")
public class MyCustomerListItemDTO {

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String name;

	@Schema(description = "전화번호")
	private String phoneNumber;

	@Schema(description = "투자 유형", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "멤버십 레벨", example = "PREMIUM")
	private MembershipLevel membershipLevel;

	@Schema(description = "보유 토큰 개수")
	private Integer token;

	/**
	 * Customer 엔티티에서 DTO 생성
	 */
	public static MyCustomerListItemDTO from(Customer customer) {
		return MyCustomerListItemDTO.builder()
			.customerId(customer.getId())
			.name(customer.getUsername())
			.phoneNumber(customer.getPhoneNumber())
			.investmentType(customer.getPrimaryInvestmentType())
			.membershipLevel(customer.getMembershipLevel())
			.token(customer.getToken())
			.build();
	}
}