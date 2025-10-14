package com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeChangeRequest;
import com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "투자 유형 변경 신청 응답 DTO")
public class ChangeRequestResponseDTO {

	@Schema(description = "신청 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String customerName;

	@Schema(description = "현재 투자 유형")
	private InvestmentType currentType;

	@Schema(description = "변경 요청 투자 유형")
	private InvestmentType requestedType;

	@Schema(description = "신청 상태")
	private ChangeRequestStatus status;

	@Schema(description = "변경 사유")
	private String reason;

	@Schema(description = "신청일")
	private LocalDate requestedDate;

	@Schema(description = "변경 예정일")
	private LocalDate targetChangeDate;

	@Schema(description = "처리일시")
	private LocalDateTime processedAt;

	@Schema(description = "처리한 트레이너 이름")
	private String approvedByName;

	@Schema(description = "거부 사유")
	private String rejectionReason;

	public static ChangeRequestResponseDTO from(InvestmentTypeChangeRequest request) {
		return ChangeRequestResponseDTO.builder()
			.id(request.getId())
			.customerId(request.getCustomer().getId())
			.customerName(request.getCustomer().getUsername())
			.currentType(request.getCurrentType())
			.requestedType(request.getRequestedType())
			.status(request.getStatus())
			.reason(request.getReason())
			.requestedDate(request.getRequestedDate())
			.targetChangeDate(request.getTargetChangeDate())
			.processedAt(request.getProcessedAt())
			.approvedByName(request.getTrainer() != null ?
				request.getTrainer().getUsername() : null)
			.rejectionReason(request.getRejectionReason())
			.build();
	}
}
