package com.tradingpt.tpt_api.domain.user.dto.response;

import com.tradingpt.tpt_api.domain.user.entity.Trainer;

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
@Schema(description = "트레이너 정보")
public class TrainerDTO {

	@Schema(description = "트레이너 ID")
	private Long trainerId;

	@Schema(description = "트레이너 프로필 URL")
	private String profileImageUrl;

	@Schema(description = "트레이너 이름")
	private String trainerName;

	public static TrainerDTO from(Trainer trainer) {
		return TrainerDTO.builder()
			.trainerId(trainer.getId())
			.trainerName(trainer.getName())
			.profileImageUrl(trainer.getProfileImageUrl())
			.build();
	}

}
