package com.whyrano.global.web.argumentresolver

import com.whyrano.global.auth.userdetails.AuthMember
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * Created by ShinD on 2022/08/13.
 */
@Component
class AuthMemberArgumentResolver : HandlerMethodArgumentResolver {


    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val hasAuthAnnotation = parameter.hasParameterAnnotation(Auth::class.java)
        val hasAuthMemberType = AuthMember::class.java.isAssignableFrom(parameter.parameterType)

        return hasAuthAnnotation && hasAuthMemberType
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): AuthMember? {

        val authentication = SecurityContextHolder.getContext().authentication

        if(authentication == null || !authentication.isAuthenticated ) return null
        return authentication.principal as AuthMember
    }
}