package com.whyrano.global.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.domain.member.entity.Role.ADMIN
import com.whyrano.domain.member.entity.Role.BASIC
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.filter.JsonLoginProcessingFilter
import com.whyrano.global.auth.filter.JwtAuthenticationFailureManager
import com.whyrano.global.auth.filter.JwtAuthenticationFilter
import com.whyrano.global.auth.filter.JwtAuthenticationManager
import com.whyrano.global.auth.handler.JsonLoginFailureHandler
import com.whyrano.global.auth.handler.JsonLoginSuccessHandler
import com.whyrano.global.auth.jwt.JwtService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.hierarchicalroles.RoleHierarchy
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.http.SessionCreationPolicy.STATELESS
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Created by ShinD on 2022/08/09.
 */
@Configuration
class SecurityConfig {

    companion object {
        const val LOGIN_URL = "/login"
        const val SIGNUP_URL = "/signup"
        const val H2_URL = "/h2-console/**"
        const val ERROR_URL = "/error"
        private val NO_CHECK_URLS = listOf(LOGIN_URL, SIGNUP_URL, H2_URL, ERROR_URL)
    }





    /**
     * Security 관련 설정
     */
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeRequests()
            .antMatchers(*NO_CHECK_URLS.toTypedArray()).permitAll()
            .antMatchers("/admin/**").hasRole(ADMIN.name)
            .anyRequest().hasRole(BASIC.name)

        http
            .formLogin().disable()
            .httpBasic().disable()//Header 에 username, password 를 실어 보내 인증하는 방식 비활성화
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(STATELESS)

        http
            .headers().frameOptions().sameOrigin()//h2-console 사용

        http
            .addFilterBefore(jsonLoginProcessingFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter(), JsonLoginProcessingFilter::class.java)

        return http.build()
    }






    /**
     * Json 형식으로 로그인을 진행하는 필터
     */
    @Bean
    fun jsonLoginProcessingFilter(
        memberService: MemberService? = null,                       // 회원 정보를 가져올때 사용
        passwordEncoder: PasswordEncoder? = null,                   // 비밀번호 일치 여부를 확인할때 사용
        jsonLoginSuccessHandler: JsonLoginSuccessHandler? = null,   // 로그인 성공시 처리
        jsonLoginFailureHandler: JsonLoginFailureHandler? = null,   // 로그인 실패시 처리
    ): JsonLoginProcessingFilter {

        //== 제대로 DI가 이루어졌는지 확인함 ==//
        checkNotNull(memberService) { "memberService is null !" }
        checkNotNull(passwordEncoder) { "passwordEncoder is null !" }
        checkNotNull(jsonLoginSuccessHandler) { "JsonLoginSuccessHandler is null !" }


        //== DaoAuthenticationProvider 설정 ==//
        val daoAuthenticationProvider = DaoAuthenticationProvider()
        daoAuthenticationProvider.setUserDetailsService(memberService)
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder)



        //== JsonLoginProcessingFilter 설정 ==//
        val jsonLoginProcessingFilter = JsonLoginProcessingFilter(LOGIN_URL)
        jsonLoginProcessingFilter.setAuthenticationManager(ProviderManager(daoAuthenticationProvider)) // ProviderManager는 DaoAuthenticationProvider 사용
        jsonLoginProcessingFilter.setAuthenticationSuccessHandler(jsonLoginSuccessHandler) // 로그인 성공 시 - jsonLoginSuccessHandler에서 처리
        jsonLoginProcessingFilter.setAuthenticationFailureHandler(jsonLoginFailureHandler) // 로그인 실패 시 - jsonLoginFailureHandler에서 처리

        return jsonLoginProcessingFilter
    }






    /**
     * Json 로그인 성공시 처리 - JWT 발급
     */
    @Bean
    fun jsonLoginSuccessHandler(jwtService: JwtService? = null): JsonLoginSuccessHandler{
        checkNotNull(jwtService) { "jwtService is null !" }
        return JsonLoginSuccessHandler(jwtService)
    }





    /**
     * Json 로그인 실패시 처리 - 예외 메세지 가공 후 반환
     */
    @Bean
    fun jsonLoginFailureHandler(objectMapper: ObjectMapper? =null): JsonLoginFailureHandler {
        checkNotNull(objectMapper) { "objectMapper is null !" }
        return JsonLoginFailureHandler(objectMapper)
    }







    /**
     * JWT를 사용하여 회원 인증을 진행하는 필터
     */
    @Bean
    fun jwtAuthenticationFilter(
       jwtAuthenticationManager: JwtAuthenticationManager? = null,
       jwtAuthenticationFailureManager: JwtAuthenticationFailureManager? = null,
    ): JwtAuthenticationFilter {
        checkNotNull(jwtAuthenticationManager) { "jwtAuthenticationManager is null !" }
        checkNotNull(jwtAuthenticationFailureManager) { "jwtAuthenticationFailureManager is null !" }

        return JwtAuthenticationFilter(NO_CHECK_URLS, jwtAuthenticationManager, jwtAuthenticationFailureManager)
    }


    /**
     * JWT를 사용하여 실제 인증을 처리
     */
    @Bean
    fun jwtAuthenticationManager(jwtService: JwtService? = null) :JwtAuthenticationManager {
        checkNotNull(jwtService) { "jwtService is null !" }

        return JwtAuthenticationManager(jwtService)
    }


    /**
     * JWT 인증 실패 시 처리
     */
    @Bean
    fun jwtAuthenticationFailureManager(objectMapper: ObjectMapper? = null) :JwtAuthenticationFailureManager {
        checkNotNull(objectMapper) { "objectMapper is null !" }
        return JwtAuthenticationFailureManager(objectMapper)
    }






    /**
     * 권한별 계층 설정
     * https://www.javafixing.com/2022/01/fixed-spring-security-role-hierarchy.html
     */
    @Bean
    fun roleHierarchy(): RoleHierarchy {
        val roleHierarchy = RoleHierarchyImpl()
        val hierarchy = "ROLE_ADMIN > ROLE_BASIC"
        roleHierarchy.setHierarchy(hierarchy)
        return roleHierarchy
    }





    /**
     * https://velog.io/@gkdud583/HttpSecurity-WebSecurity%EC%9D%98-%EC%B0%A8%EC%9D%B4
     * WebSecurity - 인증,인가 모두 처리 X
     * HttpSecurity - antMatchers에 있는 endpoint에 대한 '인증'을 무시한다.
     */
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer
            = WebSecurityCustomizer { it.ignoring().antMatchers("/images/**", "/js/**", "/webjars/**") }





    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder() //bcrypt 사용
    }
}