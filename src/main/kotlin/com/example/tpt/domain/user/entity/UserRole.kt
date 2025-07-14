package com.example.tpt.domain.user.entity

enum class UserRole(val description: String) {
	USER("일반 사용자"),
	ADMIN("관리자"),
	MANAGER("매니저");

	fun hasPermission(permission: String): Boolean {
		return when (this) {
			ADMIN -> true // 관리자는 모든 권한
			MANAGER -> permission in listOf("PROJECT_MANAGE", "TASK_MANAGE")
			USER -> permission in listOf("PROJECT_VIEW", "TASK_VIEW")
		}
	}
}
