package com.whyrano.global.config

import com.whyrano.domain.member.entity.Role.ADMIN
import com.whyrano.domain.member.entity.Role.BASIC
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.filter.JsonLoginProcessingFilter
import com.whyrano.global.auth.filter.JwtAuthenticationFilter
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




    @Bean
    fun jsonLoginProcessingFilter(
            memberService: MemberService? = null,
            passwordEncoder: PasswordEncoder? = null,
            JsonLoginSuccessHandler: JsonLoginSuccessHandler? = null,
    ): JsonLoginProcessingFilter {

        val daoAuthenticationProvider = DaoAuthenticationProvider()
        daoAuthenticationProvider.setUserDetailsService(memberService)
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder)


        val jsonLoginProcessingFilter = JsonLoginProcessingFilter(LOGIN_URL)
        jsonLoginProcessingFilter.setAuthenticationManager(ProviderManager(daoAuthenticationProvider))
        JsonLoginSuccessHandler?.let { jsonLoginProcessingFilter.setAuthenticationSuccessHandler(it) }
        return jsonLoginProcessingFilter
    }

    @Bean
    fun jsonLoginSuccessHandler(jwtService: JwtService? = null)
        = jwtService?.let { JsonLoginSuccessHandler(it) }


    @Bean
    fun jwtAuthenticationFilter(jwtService: JwtService? = null) =
        jwtService?.let { JwtAuthenticationFilter(NO_CHECK_URLS, it) }





    /**
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