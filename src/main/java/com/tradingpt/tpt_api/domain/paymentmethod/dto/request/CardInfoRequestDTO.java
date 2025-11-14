package com.tradingpt.tpt_api.domain.paymentmethod.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카드 정보 요청 DTO (비인증 빌키 발급용)
 * ⚠️ 보안 주의: 카드 정보는 절대 로깅하거나 DB에 저장하면 안 됩니다.
 */
@Getter
@NoArgsConstructor
@Schema(description = "카드 정보 요청 DTO (비인증 빌키 발급)")
public class CardInfoRequestDTO {

	@NotBlank(message = "카드번호는 필수입니다.")
	@Pattern(regexp = "\\d{13,16}", message = "카드번호는 13~16자리 숫자여야 합니다.")
	@Schema(description = "카드번호 (13~16자리)", example = "1234567890123456", requiredMode = Schema.RequiredMode.REQUIRED)
	private String cardNo;

	@NotBlank(message = "유효기간(년도)은 필수입니다.")
	@Pattern(regexp = "\\d{2}", message = "유효기간(년도)은 2자리 숫자여야 합니다.")
	@Schema(description = "유효기간 년도 (YY)", example = "25", requiredMode = Schema.RequiredMode.REQUIRED)
	private String expYear;

	@NotBlank(message = "유효기간(월)은 필수입니다.")
	@Pattern(regexp = "(0[1-9]|1[0-2])", message = "유효기간(월)은 01~12 형식이어야 합니다.")
	@Schema(description = "유효기간 월 (MM)", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
	private String expMonth;

	@NotBlank(message = "생년월일 또는 사업자번호는 필수입니다.")
	@Size(min = 6, max = 10, message = "생년월일은 6자리, 사업자번호는 10자리여야 합니다.")
	@Schema(description = "생년월일(YYMMDD) 또는 사업자번호(10자리)", example = "900101", requiredMode = Schema.RequiredMode.REQUIRED)
	private String idNo;

	@NotBlank(message = "카드 비밀번호 앞 2자리는 필수입니다.")
	@Pattern(regexp = "\\d{2}", message = "카드 비밀번호는 2자리 숫자여야 합니다.")
	@Schema(description = "카드 비밀번호 앞 2자리", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
	private String cardPw;

	/**
	 * 카드 정보를 EncData 형식의 평문으로 변환
	 * 형식: CardNo={카드번호}&ExpYear={년도}&ExpMonth={월}&IDNo={생년월일}&CardPw={비밀번호}
	 *
	 * @return EncData 평문
	 */
	public String toEncDataPlainText() {
		return String.format("CardNo=%s&ExpYear=%s&ExpMonth=%s&IDNo=%s&CardPw=%s",
			cardNo, expYear, expMonth, idNo, cardPw);
	}

	/**
	 * 보안을 위한 toString() 오버라이드
	 * 카드 정보가 로그에 노출되지 않도록 마스킹 처리
	 */
	@Override
	public String toString() {
		return "CardInfoRequestDTO{" +
			"cardNo='" + maskCardNo() + '\'' +
			", expYear='**'" +
			", expMonth='**'" +
			", idNo='******'" +
			", cardPw='**'" +
			'}';
	}

	/**
	 * 카드번호 마스킹 (앞 6자리 + **** + 뒤 4자리)
	 */
	private String maskCardNo() {
		if (cardNo == null || cardNo.length() < 10) {
			return "****";
		}
		return cardNo.substring(0, 6) + "****" + cardNo.substring(cardNo.length() - 4);
	}
}
