package com.whyrano.global.exception

/**
 * Created by ShinD on 2022/08/13.
 */
abstract class BaseException
    : RuntimeException() {
    abstract fun exceptionType(): BaseExceptionType
}