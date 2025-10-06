package com.tradingpt.tpt_api.domain.user.entity;

import com.tradingpt.tpt_api.domain.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "trainer")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DiscriminatorValue(value = "ROLE_TRAINER")
public class Trainer extends User {

	/**
	 * 필드
	 */
	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "trainer_name")
	private String trainerName;

	@Column(name = "oneline_introduction")
	private String oneLineIntroduction;

	// ⭐ getRole() 구현
	@Override
	public Role getRole() {
		return Role.ROLE_TRAINER;
	}

}
