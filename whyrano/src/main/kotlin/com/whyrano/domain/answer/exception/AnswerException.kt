package com.whyrano.domain.answer.exception

import com.whyrano.global.exception.BaseException

/**
 * Created by ShinD on 2022/08/20.
 */
class AnswerException(

    private val exceptionType: AnswerExceptionType,

    ) : BaseException() {

    override fun exceptionType() = exceptionType
}