package com.whyrano.domain.member.entity

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
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

        fun create(
            email: String,
            accessTokenExpirationPeriodDay: Long,
            algorithm: Algorithm,
        ): AccessToken =
            AccessToken(
                accessToken = JWT.create()
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .withClaim(MEMBER_EMAIL_CLAIM, email)
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

    fun getEmail(algorithm: Algorithm) =
        JWT.require(algorithm).build().verify(accessToken).getClaim(MEMBER_EMAIL_CLAIM).toString()
            .replace("\"", "") // ""hui@na.com"" 이런 식으로 반환되어 이를 제거함

}