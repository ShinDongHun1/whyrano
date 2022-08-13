package com.whyrano.global.exception

import org.springframework.http.HttpStatus

/**
 * Created by ShinD on 2022/08/13.
 */
interface BaseExceptionType {
    fun errorCode(): Int

    fun httpStatus(): HttpStatus

    fun message(): String
}