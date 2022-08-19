package com.whyrano.global.web.argumentresolver.auth

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Created by ShinD on 2022/08/13.
 */
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Auth
