package com.whyrano.global.auth.jwt

import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.entity.Token
import org.springframework.security.core.userdetails.UserDetails
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

    fun createAccessAndRefreshToken(userDetails: UserDetails): TokenDto

    fun extractUserDetail(accessToken: AccessToken): UserDetails?

    fun extractToken(request: HttpServletRequest): TokenDto?

    fun isValid(token: Token): Boolean

    fun isValidMoreThanMinute(accessToken: AccessToken, minute: Long): Boolean
    fun findMemberByTokens(accessToken: AccessToken, refreshToken: RefreshToken): Member?
}