package com.whyrano.global.auth.exception

import com.whyrano.global.exception.BaseExceptionType
import org.springframework.security.core.AuthenticationException

/**
 * Created by ShinD on 2022/08/13.
 */
class AuthException(

    private val authExceptionType: AuthExceptionType,

    ) : AuthenticationException(authExceptionType.message()) {

    fun exceptionType(): BaseExceptionType = authExceptionType
}