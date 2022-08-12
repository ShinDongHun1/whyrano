package com.whyrano.domain.member.entity

import com.auth0.jwt.algorithms.Algorithm
import com.whyrano.domain.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Created by ShinD on 2022/08/11.
 */
internal class AccessTokenTest {

    private val algorithm = Algorithm.HMAC512("jwtProperties.secretKey")


    @Test
    fun `AccessToken 권한, 이메일 생성 성공`() {

        val authMember = MemberFixture.authMember()

        val accessToken = AccessToken.create(
            id = authMember.id,
            email = authMember.username,
            role = authMember.role,
            accessTokenExpirationPeriodDay = 30,
            algorithm = algorithm
        )

        val extractedUserDetails = accessToken.getAuthMember(algorithm)

        assertThat(authMember!!.email).isEqualTo(authMember.username)
        assertThat(authMember.authorities).containsAll(authMember.authorities)
    }
}