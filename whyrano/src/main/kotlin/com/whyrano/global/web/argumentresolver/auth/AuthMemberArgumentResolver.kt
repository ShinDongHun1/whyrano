package com.whyrano.global.web.argumentresolver.auth

import com.whyrano.global.auth.userdetails.AuthMember
import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

/**
 * 인증된 회원 정보 가져오는데 사용
 *
 * Created by ShinD on 2022/08/13.
 */
@Component
class AuthMemberArgumentResolver : HandlerMethodArgumentResolver {


    override fun supportsParameter(parameter: MethodParameter): Boolean {

        // @Auth 붙어있는지 여부
        val hasAuthAnnotation = parameter.hasParameterAnnotation(Auth::class.java)

        // AuthMember에 할당 가능한지 여부
        val hasAuthMemberType = AuthMember::class.java.isAssignableFrom(parameter.parameterType)

        // @Auth authMember: AuthMember
        return hasAuthAnnotation && hasAuthMemberType
    }


    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): AuthMember? {

        //SecurityContext 로부터 인증 정보 가져오기
        val authentication = SecurityContextHolder.getContext().authentication

        // 인증 정보가 없는 경우 null 반환
        if (authentication == null || ! authentication.isAuthenticated) return null

        // 인증 객체 반환
        return authentication.principal as AuthMember
    }
}