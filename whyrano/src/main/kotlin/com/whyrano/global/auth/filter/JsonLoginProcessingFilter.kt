package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.global.auth.exception.AuthException
import com.whyrano.global.auth.exception.AuthExceptionType
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
class JsonLoginProcessingFilter(
    loginUrl: String
) : AbstractAuthenticationProcessingFilter(AntPathRequestMatcher(loginUrl)) {   // 로그인 처리 하지 않을 url 설정

    companion object {
        private const val NO_CONTENT = -1 // 내용이 없는 경우 length는 -1을 반환
        private val objectMapper: ObjectMapper = ObjectMapper()  // ObjectMapper는 Thread Safe
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {

        // 메서드가 Post 가 아닌 경우 로그인 시도하지 않음
        if(!isPost(request)) throw AuthException(AuthExceptionType.NOT_ALLOWED_LOGIN_METHOD)

        // Json 이 아닌 경우 로그인 시도하지 않음
        if(!isJson(request)) throw AuthException(AuthExceptionType.UNSUPPORTED_LOGIN_MEDIA_TYPE)

        // body에 아무것도 작성되지 않았다면 로그인 실패
        if (request.contentLength == NO_CONTENT) throw AuthException(AuthExceptionType.BAD_USERNAME_PASSWORD)


        /**
         * 로그인 처리 로직
         */
        // request로부터 계정 정보 추출
        val accountDto = extractAccount(request)

        // 공백이 있다면 처리하지 않음
        if (usernameIsBlank(accountDto) || passwordIsBlank(accountDto) ) throw AuthException(AuthExceptionType.BAD_USERNAME_PASSWORD)


        return authenticationManager.authenticate(UsernamePasswordAuthenticationToken(accountDto.username, accountDto.password));
    }

    private fun extractAccount(request: HttpServletRequest): AccountDto {

        return try {

            objectMapper.readValue(request.reader, AccountDto::class.java) // Json 파싱 중 오류가 발생할 수 있으므로 예외 처리
        }
        catch (e: Exception) {

            throw AuthException(AuthExceptionType.BAD_USERNAME_PASSWORD)
        }
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
        var password: String? = null,
    )
}
