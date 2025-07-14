package com.example.tpt.domain.user.entity

enum class UserStatus(val description: String) {
	ACTIVE("활성"),
	INACTIVE("비활성"),
	DELETED("삭제됨");

	fun canLogin(): Boolean = this == ACTIVE
}
