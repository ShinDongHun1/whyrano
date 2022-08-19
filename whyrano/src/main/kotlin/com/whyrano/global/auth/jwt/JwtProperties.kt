package com.whyrano.global.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Created by ShinD on 2022/08/10.
 */
@ConstructorBinding
@ConfigurationProperties("jwt")
data class JwtProperties (

    val secretKey: String,

    val accessTokenExpirationPeriodDay: Long,

    val refreshTokenExpirationPeriodDay: Long,

)