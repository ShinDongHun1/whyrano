package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ShinD on 2022/08/09.
 */
class JsonLoginProcessingFilter(loginUrl: String) : AbstractAuthenticationProcessingFilter(
    AntPathRequestMatcher(loginUrl)
) {


    companion object {
        private const val NO_CONTENT = -1
        // ObjectMapper 는 Thread Safe 하다.
        private val objectMapper: ObjectMapper = ObjectMapper()
    }


    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {

        if(!isJson(request)) { // Json 이 아닌 경우 로그인 시도하지 않음
            throw AuthenticationServiceException("Authentication content-type not supported: ${request.contentType}")
        }
        if(!isPost(request)) { // 메서드가 Post 가 아닌 경우 로그인 시도하지 않음
            throw AuthenticationServiceException("Authentication method not supported: ${request.method}")
        }
        if (request.contentLength == NO_CONTENT) {  // body에 아무것도 작성되지 않았다면 로그인 실패
            throw AuthenticationServiceException("Authentication request-body is null")
        }

        val accountDto = objectMapper.readValue(request.reader, AccountDto::class.java)

        if (usernameIsBlank(accountDto) || passwordIsBlank(accountDto) ) {
            throw AuthenticationServiceException("Username or Password is empty")
        }

        return authenticationManager.authenticate(UsernamePasswordAuthenticationToken(accountDto.username, accountDto.password));
    }




    private fun usernameIsBlank(accountDto: AccountDto) =
        accountDto.username == null || accountDto.username!!.isBlank()

    private fun passwordIsBlank(accountDto: AccountDto) =
        accountDto.password == null || accountDto.password!!.isBlank()


    private fun isJson(request: HttpServletRequest) =
        APPLICATION_JSON_VALUE == request.contentType

    private fun isPost(request: HttpServletRequest) =
        POST.name == request.method



    private data class AccountDto(
        var username: String? = null,
        var password: String? = null)
}
