package com.example.tpt.global.exception

import com.example.tpt.common.exception.code.BaseCode
import com.example.tpt.common.exception.code.BaseCodeInterface

open class BaseException(
	private val errorCode: BaseCodeInterface
) : RuntimeException() {

	fun getErrorCode(): BaseCode {
		return errorCode.getCode()
	}
}