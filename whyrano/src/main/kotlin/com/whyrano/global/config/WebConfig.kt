package com.whyrano.global.config

import com.whyrano.global.web.argumentresolver.auth.AuthMemberArgumentResolver
import com.whyrano.global.web.argumentresolver.pageable.Start1PageableArgumentResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Created by ShinD on 2022/08/13.
 */

//@EnableWebMvc
@Configuration
class WebConfig(

    private val authMemberArgumentResolver: AuthMemberArgumentResolver,

    private val start1PageableArgumentResolver: Start1PageableArgumentResolver,

    ) : WebMvcConfigurer { // WebMvcConfigurationSupport 와의 차이가 뭘까..? -> http://honeymon.io/tech/2018/03/13/spring-boot-mvc-controller.html

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {

        super.addArgumentResolvers(resolvers)

        resolvers.add(authMemberArgumentResolver)
        resolvers.add(start1PageableArgumentResolver)
    }
}