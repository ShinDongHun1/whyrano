package com.whyrano.global.auth.filter

import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ShinD on 2022/08/13.
 */
class JwtAuthenticationFilter(
    uncheckedUrls: List<String> = emptyList(),
    private val jwtAuthenticationManager: JwtAuthenticationManager,
    private val jwtAuthenticationFailureManager: JwtAuthenticationFailureManager,
) : Filter {

    private val DEFAULT_ANT_PATH_REQUEST_MATCHERS: List<AntPathRequestMatcher> = uncheckedUrls.map { AntPathRequestMatcher(it) }

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        val req = request as HttpServletRequest
        val res = response as HttpServletResponse

        // permitAll 인 경우에는 토큰을 검사하지 않음
        for (matcher in DEFAULT_ANT_PATH_REQUEST_MATCHERS) {
            if (matcher.matches(request)) {
                return chain.doFilter(request, response)
            }
        }

        try {
            //인증 진행
            jwtAuthenticationManager.authenticateWithTokens(req, res, chain)
        }
        catch (ex: Exception) {
            //예외 시 후처리
            jwtAuthenticationFailureManager.failureAuthentication(res, ex)
        }
    }
}

