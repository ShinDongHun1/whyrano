package com.whyrano.global.auth.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.global.auth.exception.AuthException
import com.whyrano.global.auth.exception.AuthExceptionType.ELSE
import com.whyrano.global.exception.ExceptionResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ShinD on 2022/08/13.
 */
class JsonLoginFailureHandler(

    private val objectMapper: ObjectMapper,

    ) : AuthenticationFailureHandler {

    private val log = KotlinLogging.logger { }


    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException,
    ) {

        //예외 정보 가져오기
        when (exception) {

            //예상한 범위 내의 오류
            is AuthException -> {
                val exceptionType = exception.exceptionType()
                setResponse(
                    response = response,
                    status = exceptionType.httpStatus(),
                    contentType = APPLICATION_JSON_VALUE,
                    charset = UTF_8,
                    content = objectMapper.writeValueAsString(
                        ExceptionResponse(
                            errorCode = exceptionType.errorCode(),
                            message = exceptionType.message()
                        )
                    )
                )
            }

            //예상하지 못한 오류
            else -> {
                log.error { exception.message }
                exception.printStackTrace()

                setResponse(
                    response = response,
                    status = UNAUTHORIZED,
                    contentType = APPLICATION_JSON_VALUE,
                    charset = UTF_8,
                    content = objectMapper.writeValueAsString(
                        ExceptionResponse(
                            errorCode = ELSE.errorCode(),
                            message = ELSE.message()
                        )
                    )
                )
            }
        }
    }


    private fun setResponse(
        response: HttpServletResponse,
        status: HttpStatus,
        contentType: String,
        charset: Charset,
        content: String,
    ) {
        response.status = status.value()
        response.contentType = contentType
        response.characterEncoding = charset.name()
        response.writer.println(content)
    }
}