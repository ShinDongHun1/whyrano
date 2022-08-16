package com.whyrano.global.auth.jwt

import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.RefreshToken

/**
 * Created by ShinD on 2022/08/09.
 */
data class TokenDto(

    val accessToken: String? = null,

    val refreshToken: String? = null,

) {

    fun accessToken(): AccessToken = AccessToken(accessToken)

    fun refreshToken(): RefreshToken = RefreshToken(refreshToken)
}
