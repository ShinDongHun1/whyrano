package com.whyrano.global.web.argumentresolver.pageable

import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.DESC
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

/**
 * Created by ShinD on 2022/08/16.
 */
@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class Page(

    val direction: Direction = DESC,

    val sort: Array<String> = ["createdDate"],

    )
