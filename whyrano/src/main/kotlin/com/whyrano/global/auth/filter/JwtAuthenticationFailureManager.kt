package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.global.auth.exception.AuthException
import com.whyrano.global.auth.exception.AuthExceptionType
import com.whyrano.global.exception.ExceptionResponse
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.servlet.http.HttpServletResponse

/**
 * Created by ShinD on 2022/08/13.
 */
class JwtAuthenticationFailureManager(
    private val objectMapper: ObjectMapper,
) {

    private val log = KotlinLogging.logger {  }

    fun failureAuthentication(response: HttpServletResponse ,ex: Exception) {
        when (ex) {
            //예상한 범위 내의 오류
            is AuthException -> {
                val exceptionType = ex.exceptionType()
                setResponse(
                    response = response,
                    status = exceptionType.httpStatus(),
                    contentType = MediaType.APPLICATION_JSON_VALUE,
                    charset = StandardCharsets.UTF_8,
                    content = objectMapper.writeValueAsString(ExceptionResponse(errorCode = exceptionType.errorCode(), message = exceptionType.message()))
                )
            }

            //예상하지 못한 오류
            else -> {
                log.error { ex.message }
                ex.printStackTrace()
                setResponse(
                    response = response,
                    status = HttpStatus.UNAUTHORIZED,
                    contentType = MediaType.APPLICATION_JSON_VALUE,
                    charset = StandardCharsets.UTF_8,
                    content = objectMapper.writeValueAsString(ExceptionResponse(errorCode = AuthExceptionType.ELSE.errorCode(), message = AuthExceptionType.ELSE.message()))
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