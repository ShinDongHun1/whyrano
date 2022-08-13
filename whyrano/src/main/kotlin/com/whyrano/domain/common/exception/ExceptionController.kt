package com.whyrano.global.exception

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Created by ShinD on 2022/08/13.
 */

@RestControllerAdvice
class ExceptionController {

    private val log = KotlinLogging.logger {  }

    @ExceptionHandler(BaseException::class)
    fun handleBaseException(ex: BaseException): ResponseEntity<ExceptionResponse> {
        val exceptionType = ex.exceptionType()
        log.error { "ErrorCode : [${exceptionType.errorCode()}], message : [${exceptionType.message()}]" }

        return ResponseEntity
            .status(exceptionType.httpStatus())
            .body(ExceptionResponse(errorCode = exceptionType.errorCode(), message = exceptionType.message()))
    }
}

data class ExceptionResponse(
    val errorCode: Int,
    val message: String,
)