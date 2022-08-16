package com.whyrano.global.config

import com.whyrano.global.web.argumentresolver.AuthMemberArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Created by ShinD on 2022/08/13.
 */
@Configuration
class WebConfig(

    private val authMemberArgumentResolver: AuthMemberArgumentResolver,

) : WebMvcConfigurer { //TODO WebMvcConfigurationSupport 와의 차이가 뭘까..?

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {

        super.addArgumentResolvers(resolvers)

        resolvers.add(authMemberArgumentResolver)
    }
}