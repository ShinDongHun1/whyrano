package com.whyrano.domain.member.exception

import com.whyrano.global.exception.BaseException
import com.whyrano.global.exception.BaseExceptionType

/**
 * Created by ShinD on 2022/08/13.
 */
class MemberException(

    private val exceptionType: MemberExceptionType

) : BaseException(){

    override fun exceptionType() = exceptionType
}