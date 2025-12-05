package com.tradingpt.tpt_api.domain.user.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "user")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING)
public abstract class User extends BaseEntity {

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PasswordHistory> passwordHistories;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long id;

	@Column(name = "username", unique = true)
	private String username; //로컬일 경우 id, 소셜일경우 KAKAO_3430803880

	@Column(name = "email")
	private String email;

	private String password;

	@Column(name = "name", nullable = false)
	private String name; //유저의 이름

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;      // LOCAL, KAKAO, NAVER

	@Column(name = "provider_id")
	private String providerId; // 소셜 id

	@Column(name = "profileImageUrl", length = 512)
	private String profileImageUrl;

	@Column(name = "profileImageKey", length = 512)
	private String profileImageKey;

	@Column(name = "nickname", length = 30)
	private String nickname;

	/**
	 * JPA Optimistic Locking을 위한 버전 필드
	 * 동시성 제어: 모든 User 계층(Customer, Trainer, Admin)에 적용
	 * - Customer: 토큰 보상 중복 지급 방지
	 * - 실제 발생 확률: 거의 0% (1인 1접근 패턴)
	 * - 방어적 프로그래밍: 예상치 못한 네트워크 재시도, 브라우저 중복 요청 대비
	 * - 비용: 거의 없음 (컬럼 1개 추가, 성능 영향 미미)
	 */
	@Version
	@Column(name = "version")
	@Builder.Default
	private Long version = 0L;

	public void changeNickname(String nickname) {
		this.nickname = nickname;
	}

	//프로필 이미지 변경
	public void changeProfileImage(String key, String url) {
		this.profileImageKey = key;
		this.profileImageUrl  = url;
	}

	public void changeName(String name) {
		this.name = name;
	}

	public void changeUsername(String username) {
		this.username = username;
	}

	public void changePassword(String password) {
		this.password = password;
	}


	// 추상 메서드로 Role 반환
	public abstract Role getRole();
}

