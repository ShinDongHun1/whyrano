package com.whyrano.domain.member.entity

import com.auth0.jwt.algorithms.Algorithm
import com.whyrano.domain.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.userdetails.User

/**
 * Created by ShinD on 2022/08/11.
 */
internal class AccessTokenTest {

    private val algorithm = Algorithm.HMAC512("jwtProperties.secretKey")


    @Test
    fun `AccessToken 권한, 이메일 생성 성공`() {

        val memberDto = MemberFixture.createMemberDto()
        val userDetails =
            User.builder().username(memberDto.email).password(memberDto.password).roles(memberDto.role.name).build()

        val accessToken = AccessToken.create(
            email = userDetails.username,
            authority = userDetails.authorities.toList()[0].toString(), // Authority는 반드시 하나임
            accessTokenExpirationPeriodDay = 30,
            algorithm = algorithm
        )

        val extractedUserDetails = accessToken.getUserDetails(algorithm)

        assertThat(extractedUserDetails!!.username).isEqualTo(userDetails.username)
        assertThat(extractedUserDetails.authorities).containsAll(userDetails.authorities)
    }
}