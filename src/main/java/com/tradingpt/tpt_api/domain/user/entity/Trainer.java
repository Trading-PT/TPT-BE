package com.tradingpt.tpt_api.domain.user.entity;

import com.tradingpt.tpt_api.domain.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
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
@PrimaryKeyJoinColumn(name = "user_id")
public class Trainer extends User {

	/**
	 * 필드
	 */

	@Column(name = "oneline_introduction")
	private String onelineIntroduction;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Override
	public Role getRole() {
		return Role.ROLE_TRAINER;
	}
	//변경 메서드
	public void changeProfileImage(String key, String url) {
		super.changeProfileImage(key,url); // 부모 User가 key 필드를 가지고 있을 경우
	}

	public void changePhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void changeOnelineIntroduction(String intro) {
		this.onelineIntroduction = intro;
	}
}
