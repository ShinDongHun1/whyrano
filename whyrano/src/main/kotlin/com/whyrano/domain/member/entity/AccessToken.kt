package com.whyrano.domain.member.entity

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.whyrano.global.auth.userdetails.AuthMember
import java.util.*
import java.util.concurrent.TimeUnit
import javax.persistence.Embeddable

/**
 * Created by ShinD on 2022/08/10.
 */
@Embeddable
data class AccessToken (
    var accessToken: String? = null,
) : Token {

    companion object {
        const val ACCESS_TOKEN_SUBJECT = "AccessToken"
        const val MEMBER_EMAIL_CLAIM = "memberEmail"
        const val MEMBER_ID_CLAIM = "memberId"
        const val MEMBER_ROLE_CLAIM = "memberRole"

        fun create(
            id: Long,
            email: String,
            role: Role,
            accessTokenExpirationPeriodDay: Long,
            algorithm: Algorithm,
        ): AccessToken =
            AccessToken(
                accessToken = JWT.create()
                    .withSubject(ACCESS_TOKEN_SUBJECT)
                    .withClaim(MEMBER_ID_CLAIM, id)
                    .withClaim(MEMBER_EMAIL_CLAIM, email)
                    .withClaim(MEMBER_ROLE_CLAIM, role.name) // role은 반드시 하나임
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

    fun getAuthMember(algorithm: Algorithm): AuthMember? {
        return try {

            val jwt = JWT.require(algorithm).build().verify(accessToken)

            val id           =   jwt.getClaim(MEMBER_ID_CLAIM).toString().replace("\"", "")
            val email        =   jwt.getClaim(MEMBER_EMAIL_CLAIM).toString().replace("\"", "") // ""hui@na.com"" 이런 식으로 반환되어 이를 제거함
            val role         =   jwt.getClaim(MEMBER_ROLE_CLAIM).toString().replace("\"", "")

            AuthMember(id = id.toLong(), email = email, role = Role.valueOf(role))
        }
        catch (ex: JWTVerificationException) {
            //토큰이 유효하지 않는 등의 예외 발생 시 -> null 반환
            null
        }
    }

    fun getExpiredDate(algorithm: Algorithm): Date =
        JWT.require(algorithm).build().verify(accessToken).expiresAt

}