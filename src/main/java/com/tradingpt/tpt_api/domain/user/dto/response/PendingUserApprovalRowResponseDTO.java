package com.tradingpt.tpt_api.domain.user.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Uid;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

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
@Schema(description = "신규 가입자 UID 승인 대기 목록 응답 DTO")
public class PendingUserApprovalRowResponseDTO {

	@Schema(description = "사용자 ID (User 고유 식별자)")
	private Long userId;

	@Schema(description = "가입자 이름")
	private String name;

	@Schema(description = "가입자 전화번호 (Customer 기준)")
	private String phone;

	@Schema(description = "승인 신청 일시 (User 생성 시각)")
	private LocalDateTime requestedAt;

	@Schema(description = "고객이 입력한 UID 정보")
	private UidInfoResponseDTO uid;

	@Schema(description = "승인 상태 (UID_REVIEW_PENDING, UID_APPROVED, UID_REJECTED 등)")
	private UserStatus status;

	@Schema(description = "트레이너 이름")
	private String trainerName;

	/** Customer 엔티티 → 화면 응답 DTO */
	public static PendingUserApprovalRowResponseDTO from(Customer c) {
		if (c == null)
			return null;

		UidInfoResponseDTO uidDto = null;
		Uid uidEntity = c.getUid();
		if (uidEntity != null) {
			uidDto = UidInfoResponseDTO.from(uidEntity);
		}

		return PendingUserApprovalRowResponseDTO.builder()
			.userId(c.getId())
			.name(c.getName())
			.phone(c.getPhoneNumber())
			.requestedAt(c.getCreatedAt())
			.uid(uidDto)
			.status(c.getUserStatus())
				.trainerName(
						c.getAssignedTrainer() != null
								? c.getAssignedTrainer().getName()
								: null
				)

				.build();
	}
}