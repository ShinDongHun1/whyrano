package com.whyrano.global.config


import com.whyrano.global.auth.jwt.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Created by ShinD on 2022/08/10.
 */

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfig {
}