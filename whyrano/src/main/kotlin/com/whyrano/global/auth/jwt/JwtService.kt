package com.whyrano.global.auth.jwt

import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.entity.Token
import com.whyrano.global.auth.userdetails.AuthMember
import javax.servlet.http.HttpServletRequest

/**
 * Created by ShinD on 2022/08/09.
 */
interface JwtService {
    companion object {
        const val ACCESS_TOKEN_BODY = "accessToken"
        const val REFRESH_TOKEN_BODY = "refreshToken"
        const val ACCESS_TOKEN_HEADER_NAME = "Authorization"
        const val ACCESS_TOKEN_HEADER_PREFIX = "Bearer "
        const val REFRESH_TOKEN_HEADER_NAME = "RefreshToken"
    }

    fun createAccessAndRefreshToken(authMember: AuthMember): TokenDto

    fun extractAuthMember(accessToken: AccessToken): AuthMember?

    fun extractToken(request: HttpServletRequest): TokenDto?

    fun isValid(token: Token): Boolean

    fun isValidMoreThanMinute(accessToken: AccessToken, minute: Long): Boolean
    fun findMemberByTokens(accessToken: AccessToken, refreshToken: RefreshToken): Member?
}