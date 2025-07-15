package com.example.tpt.domain.user.entity

import com.example.tpt.global.common.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(
	@Column(nullable = false, unique = true)
	var email: String,

	@Column(nullable = false)
	var password: String,

	@Column(nullable = false)
	var name: String,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var role: UserRole = UserRole.USER,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	var status: UserStatus = UserStatus.ACTIVE,

	@Column
	var lastLoginAt: LocalDateTime? = null,

	@Column
	var profileImageUrl: String? = null,

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long? = null
) : BaseEntity() {

	// 비즈니스 메서드들
	fun login() {
		if (status != UserStatus.ACTIVE) {
			throw IllegalStateException("비활성화된 사용자입니다.")
		}
		this.lastLoginAt = LocalDateTime.now()
	}

	fun updateProfile(name: String, profileImageUrl: String?) {
		this.name = name
		this.profileImageUrl = profileImageUrl
	}

	fun changePassword(newPassword: String) {
		// 실제로는 암호화된 비밀번호 비교 로직이 필요
		this.password = newPassword
	}

	fun deactivate() {
		if (status == UserStatus.DELETED) {
			throw IllegalStateException("이미 삭제된 사용자입니다.")
		}
		this.status = UserStatus.INACTIVE
	}

	fun delete() {
		this.status = UserStatus.DELETED
	}

	fun hasRole(role: UserRole): Boolean {
		return this.role == role
	}

	fun isActive(): Boolean {
		return status == UserStatus.ACTIVE
	}

	companion object {
		fun create(
			email: String,
			password: String,
			name: String,
			role: UserRole = UserRole.USER
		): User {
			// 도메인 규칙 검증
			require(email.contains("@")) { "올바른 이메일 형식이 아닙니다." }
			require(password.length >= 8) { "비밀번호는 8자 이상이어야 합니다." }
			require(name.isNotBlank()) { "이름은 필수입니다." }

			return User(
				email = email,
				password = password, // 실제로는 암호화 필요
				name = name,
				role = role
			)
		}
	}
}
