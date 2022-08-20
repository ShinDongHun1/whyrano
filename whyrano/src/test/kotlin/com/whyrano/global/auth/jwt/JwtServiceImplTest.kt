package com.whyrano.global.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.ACCESS_TOKEN_EXPIRATION_PERIOED_DAY
import com.whyrano.domain.member.fixture.MemberFixture.REFRESH_TOKEN_EXPIRATION_PERIOED_DAY
import com.whyrano.domain.member.fixture.MemberFixture.SECRRT_KEY
import com.whyrano.domain.member.fixture.MemberFixture.authMember
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_NAME
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_PREFIX
import com.whyrano.global.auth.jwt.JwtService.Companion.REFRESH_TOKEN_HEADER_NAME
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import java.lang.System.currentTimeMillis
import java.util.*
import java.util.concurrent.TimeUnit.DAYS
import java.util.concurrent.TimeUnit.MILLISECONDS

/**
 * Created by ShinD on 2022/08/10.
 */
@ExtendWith(MockKExtension::class)
internal class JwtServiceImplTest {


    @MockK
    private lateinit var memberRepository: MemberRepository

    @MockK
    private lateinit var jwtProperties: JwtProperties

    @MockkBean
    private lateinit var jwtService: JwtService

    private lateinit var algorithm: Algorithm




    @BeforeEach
    private fun setUp() {
        every { jwtProperties.secretKey } returns SECRRT_KEY
        every { jwtProperties.refreshTokenExpirationPeriodDay } returns REFRESH_TOKEN_EXPIRATION_PERIOED_DAY
        every { jwtProperties.accessTokenExpirationPeriodDay } returns ACCESS_TOKEN_EXPIRATION_PERIOED_DAY

        algorithm = Algorithm.HMAC512(jwtProperties.secretKey)

        jwtService = JwtServiceImpl(memberRepository, jwtProperties)
    }



    @Test
    @DisplayName("회원 인증 정보로부터 AccessToken과 RefreshToken 생성 성공")
    fun test_createAccessAndRefreshToken_success() {
        //given
        val member = MemberFixture.member()
        every { memberRepository.findByEmail(member.email) } returns member
        val authMember = authMember()

        //when
        val tokenDto = jwtService.createAccessAndRefreshToken(authMember)


        // AccessToken 만료일 검증
        assertThat(JWT.require(algorithm).build().verify(tokenDto.accessToken).expiresAt)
            .isAfter(
                Date(
                    MILLISECONDS.convert(jwtProperties.accessTokenExpirationPeriodDay, DAYS)
                        .plus(currentTimeMillis())
                        .minus(MILLISECONDS.convert(1, DAYS))
                )
            )

        // RefreshToken 만료일 검증
        assertThat(JWT.require(algorithm).build().verify(tokenDto.refreshToken).expiresAt)
            .isAfter(
                Date(
                    MILLISECONDS.convert(jwtProperties.refreshTokenExpirationPeriodDay, DAYS)
                        .plus(currentTimeMillis())
                        .minus(MILLISECONDS.convert(1, DAYS))
                )
            )
    }



    @Test
    @DisplayName("AccessToken으로부터 userDetail 추출 성공")
    fun test_extractMemberEmail_success() {
        //given
        val member = MemberFixture.authMember()
        val accessToken = AccessToken.create(
            member.id,
            member.email,
            member.role,
            jwtProperties.accessTokenExpirationPeriodDay,
            algorithm
        )


        //when, then
        assertThat(jwtService.extractAuthMember(accessToken) !!.username).isEqualTo(member.username)
        assertThat(jwtService.extractAuthMember(accessToken) !!.authorities).isEqualTo(member.authorities)
    }



    @Test
    @DisplayName("Token 유효성 검사 성공")
    fun test_isValid_success() {
        //given
        val invalidToken = RefreshToken.create(- 1, algorithm)
        val validToken = RefreshToken.create(1, algorithm)


        //when, then
        assertThat(jwtService.isValid(invalidToken)).isFalse
        assertThat(jwtService.isValid(validToken)).isTrue
    }



    @Test
    @DisplayName("request로부터 토큰 추출 성공")
    fun test_extractToken_success() {
        //given
        val member = MemberFixture.authMember()
        val accessToken = AccessToken.create(
            member.id,
            member.email,
            member.role,
            jwtProperties.accessTokenExpirationPeriodDay,
            algorithm
        )

        val refreshToken = RefreshToken.create(jwtProperties.refreshTokenExpirationPeriodDay, algorithm)

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader(
            ACCESS_TOKEN_HEADER_NAME,
            ACCESS_TOKEN_HEADER_PREFIX + accessToken.accessToken !!
        )
        mockHttpServletRequest.addHeader(REFRESH_TOKEN_HEADER_NAME, refreshToken.refreshToken !!)


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        extractToken !!
        assertThat(extractToken.accessToken).isNotNull
        assertThat(extractToken.refreshToken).isNotNull
    }



    @Test
    @DisplayName("request로부터 토큰 추출 실패 - Access Token이 없는 경우")
    fun `request로부터 토큰 추출 실패 - Access Token이 없는 경우`() {
        //given
        val refreshToken = RefreshToken.create(jwtProperties.refreshTokenExpirationPeriodDay, algorithm)

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader(REFRESH_TOKEN_HEADER_NAME, refreshToken.refreshToken !!)


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        assertThat(extractToken).isNull()
    }



    @Test
    @DisplayName("request로부터 토큰 추출 실패 - Refresh Token이 없는 경우")
    fun `request로부터 토큰 추출 실패 - Refresh Token이 없는 경우`() {
        //given
        val member = MemberFixture.authMember()
        val accessToken = AccessToken.create(
            member.id,
            member.email,
            member.role,
            jwtProperties.accessTokenExpirationPeriodDay,
            algorithm
        )


        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader(
            ACCESS_TOKEN_HEADER_NAME,
            ACCESS_TOKEN_HEADER_PREFIX + accessToken.accessToken !!
        )


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        assertThat(extractToken).isNull()
    }



    @Test
    @DisplayName("request로부터 토큰 추출 실패 - 두 토큰 모두 없는 경우")
    fun `request로부터 토큰 추출 실패 - 두 토큰 모두 없는 경우`() {
        //given
        val mockHttpServletRequest = MockHttpServletRequest()


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        assertThat(extractToken).isNull()
    }



    @Test
    @DisplayName("request로부터 토큰 추출 실패 - AccessToken 앞에 Bearer이 없는 경우")
    fun `request로부터 토큰 추출 실패 - AccessToken 앞에 Bearer이 없는 경우`() {
        //given
        val member = MemberFixture.authMember()
        val accessToken = AccessToken.create(
            member.id,
            member.email,
            member.role,
            jwtProperties.accessTokenExpirationPeriodDay,
            algorithm
        )


        val refreshToken = RefreshToken.create(jwtProperties.refreshTokenExpirationPeriodDay, algorithm)

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader(ACCESS_TOKEN_HEADER_NAME, accessToken.accessToken !!)
        mockHttpServletRequest.addHeader(REFRESH_TOKEN_HEADER_NAME, refreshToken.refreshToken !!)


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        assertThat(extractToken).isNull()
    }



    @Test
    @DisplayName("request로부터 토큰 추출 실패 - AccessToken의 HeaderName이 Authorization이 아닌 경우")
    fun `request로부터 토큰 추출 실패 - AccessToken의 HeaderName이 Authorization이 아닌 경우`() {
        //given
        val member = MemberFixture.authMember()
        val accessToken = AccessToken.create(
            member.id,
            member.email,
            member.role,
            jwtProperties.accessTokenExpirationPeriodDay,
            algorithm
        )


        val refreshToken = RefreshToken.create(jwtProperties.refreshTokenExpirationPeriodDay, algorithm)

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader(
            ACCESS_TOKEN_HEADER_NAME + "NO",
            ACCESS_TOKEN_HEADER_PREFIX + accessToken.accessToken !!
        )
        mockHttpServletRequest.addHeader(REFRESH_TOKEN_HEADER_NAME, refreshToken.refreshToken !!)


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        assertThat(extractToken).isNull()
    }



    @Test
    @DisplayName("request로부터 토큰 추출 실패 - RefreshToken HeaderName이 RefreshToken이 아닌 경우")
    fun `request로부터 토큰 추출 실패 - RefreshToken HeaderName이 RefreshToken이 아닌 경우`() {
        //given
        val member = MemberFixture.authMember()
        val accessToken = AccessToken.create(
            member.id,
            member.email,
            member.role,
            jwtProperties.accessTokenExpirationPeriodDay,
            algorithm
        )


        val refreshToken = RefreshToken.create(jwtProperties.refreshTokenExpirationPeriodDay, algorithm)

        val mockHttpServletRequest = MockHttpServletRequest()
        mockHttpServletRequest.addHeader(
            ACCESS_TOKEN_HEADER_NAME,
            ACCESS_TOKEN_HEADER_PREFIX + accessToken.accessToken !!
        )
        mockHttpServletRequest.addHeader(REFRESH_TOKEN_HEADER_NAME + "NO", refreshToken.refreshToken !!)


        //when
        val extractToken = jwtService.extractToken(mockHttpServletRequest)


        //then
        assertThat(extractToken).isNull()
    }
}