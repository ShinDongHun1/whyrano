package com.whyrano.domain.member.entity

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.Embeddable

/**
 * Created by ShinD on 2022/08/10.
 */
@Embeddable
class AccessToken (
    var accessToken: String? = null,
) : Token {

    companion object {
        const val ACCESS_TOKEN_SUBJECT = "AccessToken"
        const val MEMBER_EMAIL_CLAIM = "memberEmail"
        const val MEMBER_AUTHORITY_CLAIM = "memberAuthority"

        fun create(
            email: String,
            authority: String,
            accessTokenExpirationPeriodDay: Long,
            algorithm: Algorithm,
        ): AccessToken =
            AccessToken(
                accessToken = JWT.create()
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .withClaim(MEMBER_EMAIL_CLAIM, email)
                    .withClaim(MEMBER_AUTHORITY_CLAIM, authority) // Authority는 반드시 하나임
                    .withExpiresAt(
                        Date(
                            TimeUnit.MILLISECONDS.convert(accessTokenExpirationPeriodDay, TimeUnit.DAYS).plus(
                                System.currentTimeMillis()
                            ))
                    )
                    .sign(algorithm)
            )
    }


    override fun isValid(algorithm: Algorithm) =
        try {
            JWT.require(algorithm).build().verify(accessToken)
            true
        }
        catch (e: Exception) { false }

    fun getUserDetails(algorithm: Algorithm): UserDetails? {
        return try {

            val jwt = JWT.require(algorithm).build().verify(accessToken)

            val email = jwt.getClaim(MEMBER_EMAIL_CLAIM).toString()
                .replace("\"", "") // ""hui@na.com"" 이런 식으로 반환되어 이를 제거함

            val authority = jwt.getClaim(MEMBER_AUTHORITY_CLAIM).toString().replace("\"", "")

            User.builder().username(email).password("SECRET").authorities(authority).build()
        }
        //토큰이 유효하지 않는 등의 예외 발생 시 -> null 반환
        catch (ex: JWTVerificationException) {
            null
        }
    }

    fun getExpiredDate(algorithm: Algorithm): Date =
        JWT.require(algorithm).build().verify(accessToken).expiresAt

}