package com.whyrano.global.exception

import mu.KotlinLogging
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Created by ShinD on 2022/08/13.
 */

@RestControllerAdvice
class ExceptionController {

    companion object {
        const val BIND_EXCEPTION_MESSAGE = "요청에 채워지지 않은 필드가 있습니다."
        const val BIND_EXCEPTION_ERROR_CODE = 9000
        val BIND_EXCEPTION_HTTP_STATUS = BAD_REQUEST

        const val UNEXPECTED_EXCEPTION_MESSAGE = "예측하지 못한 예외가 발생했습니다."
        const val UNEXPECTED_EXCEPTION_ERROR_CODE = 9999
        val UNEXPECTED_EXCEPTION_HTTP_STATUS = INTERNAL_SERVER_ERROR
    }

    private val log = KotlinLogging.logger {  }





    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ExceptionResponse> {

        val exceptionType = ex.exceptionType()

        log.error { "ErrorCode : [${exceptionType.errorCode()}], message : [${exceptionType.message()}]" }

        return ResponseEntity
            .status(exceptionType.httpStatus())
            .body(ExceptionResponse(errorCode = exceptionType.errorCode(), message = exceptionType.message()))
    }





    @ExceptionHandler(BindException::class, HttpMessageNotReadableException::class)
    fun handleBindException(ex: Exception): ResponseEntity<ExceptionResponse> {

        log.error { "Json 혹은 요청 파라미터의 형식이 올바르지 않습니다. - message : [${ex.message}], cause : [${ex.cause}]" }

        return ResponseEntity
            .status(BIND_EXCEPTION_HTTP_STATUS)
            .body(ExceptionResponse(errorCode = BIND_EXCEPTION_ERROR_CODE, message = BIND_EXCEPTION_MESSAGE))
    }





    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ExceptionResponse> {

        log.error { "예측하지 못한 예외 발생 - message : [${ex.message}], cause : [${ex.cause}]" }

        ex.printStackTrace()

        return ResponseEntity
            .status(UNEXPECTED_EXCEPTION_HTTP_STATUS)
            .body(ExceptionResponse(errorCode = UNEXPECTED_EXCEPTION_ERROR_CODE, message = UNEXPECTED_EXCEPTION_MESSAGE))
    }
}




data class ExceptionResponse(
    val errorCode: Int,
    val message: String,
)