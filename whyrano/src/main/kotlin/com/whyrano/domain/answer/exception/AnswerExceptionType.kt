package com.whyrano.domain.answer.exception

import com.whyrano.global.exception.BaseExceptionType
import org.springframework.http.HttpStatus

/**
 * Created by ShinD on 2022/08/20.
 */
enum class AnswerExceptionType(

    private val errorCode: Int,

    private val httpStatus: HttpStatus,

    private val message: String

) : BaseExceptionType {

    ;

    override fun errorCode() = errorCode

    override fun httpStatus() = httpStatus

    override fun message() = message
}