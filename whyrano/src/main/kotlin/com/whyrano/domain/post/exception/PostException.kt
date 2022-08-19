package com.whyrano.domain.post.exception

import com.whyrano.global.exception.BaseException

/**
 * Created by ShinD on 2022/08/14.
 */
class PostException (

    private val exceptionType: PostExceptionType

) : BaseException(){

    override fun exceptionType() = exceptionType
}