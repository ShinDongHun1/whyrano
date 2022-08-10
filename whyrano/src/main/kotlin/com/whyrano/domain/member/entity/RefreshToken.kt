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
class RefreshToken (
    var refreshToken: String? =null
) : Token {
    companion object {
        private const val REFRESH_TOKEN_SUBJECT = "RefreshToken"

        fun create(
            refreshTokenExpirationPeriodDay: Long,
            algorithm: Algorithm,
        ): RefreshToken =
            RefreshToken(
                refreshToken = JWT.create()
                    .withSubject(RefreshToken.REFRESH_TOKEN_SUBJECT)
                    .withExpiresAt(Date(
                        TimeUnit.MILLISECONDS.convert(refreshTokenExpirationPeriodDay, TimeUnit.DAYS).plus(
                            System.currentTimeMillis()
                        )))
                    .sign(algorithm)
            )
    }


    override fun isValid(algorithm: Algorithm)
            = try {
        JWT.require(algorithm).build().verify(refreshToken)
        true
    } catch (e: Exception) { false }
}