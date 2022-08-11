package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.fixture.MemberFixture.accessToken
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.fixture.MemberFixture.refreshToken
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_NAME
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_PREFIX
import com.whyrano.global.auth.jwt.JwtService.Companion.REFRESH_TOKEN_HEADER_NAME
import com.whyrano.global.auth.jwt.JwtServiceImpl
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.JwtConfig
import com.whyrano.global.config.SecurityConfig
import io.mockk.every
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ShinD on 2022/08/11.
 */
@WebMvcTest
@Import(JwtServiceImpl::class, JwtConfig::class, JwtServiceImpl::class, SecurityConfig::class)
internal class JwtAuthenticationFilterTest {


    private val objectMapper = ObjectMapper()
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var jwtService: JwtService


    @Test
    @DisplayName("permitAll 요청 url인 경우 처리 X")
    fun test_permitAll_no_filtering() {
        //given
        mockMvc
            .perform(get("/login"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("토큰이 존재하지 않는 경우 403")
    fun test_no_token_403() {
        //given
        mockMvc
            .perform(get("/test"))
            .andExpect(status().isForbidden)
    }


    @Test
    @DisplayName("Access 토큰 하나만 존재하는 경우 403")
    fun test_only_access_token_403() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        val tokenDto = createTokenDto(accessToken())
        mockMvc
            .perform(get("/test")
                .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
            )
            .andExpect(status().isForbidden)
    }


    private fun createTokenDto(accessToken: AccessToken? = null, refreshToken: RefreshToken? = null): TokenDto
        = TokenDto(accessToken = accessToken?.accessToken, refreshToken =refreshToken?.refreshToken)


    @Test
    @DisplayName("Refresh 토큰 하나만 존재하는 경우 403")
    fun test_only_refresh_token_403() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        val tokenDto = createTokenDto(refreshToken = refreshToken())
        mockMvc
            .perform(get("/test")
                .header(REFRESH_TOKEN_HEADER_NAME,   tokenDto.refreshToken)
            )
            .andExpect(status().isForbidden)
    }




    @Test
    @DisplayName("Access 토큰에 Bearer이 안 붙은 경우 - (AccessToken,RefreshToken 모두 정상 ) - 403")
    fun `Access 토큰에 Bearer이 안 붙은 경우 - (AccessToken,RefreshToken 모두 정상 ) - 403`() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        val tokenDto = createTokenDto(accessToken(), refreshToken())
        mockMvc
            .perform(get("/test")
                .header(ACCESS_TOKEN_HEADER_NAME,  tokenDto.accessToken)
                .header(REFRESH_TOKEN_HEADER_NAME,   tokenDto.refreshToken)
            )
            .andExpect(status().isForbidden)
    }


    @Test
    @DisplayName("Access 토큰이 만료된 경우 - (AccessToken,RefreshToken 모두 정상 ) - 200에 토큰 재발급")
    fun `Access 토큰이 만료된 경우 - (AccessToken,RefreshToken 모두 정상 ) - 200에 토큰 재발급`() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        every { memberRepository.findByAccessTokenAndRefreshToken(any(), any()) } returns member()
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken())

        val andReturn = mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isOk)
            .andReturn()

        val result = objectMapper.readValue(andReturn.response.contentAsString, TokenDto::class.java)
        Assertions.assertThat(result.accessToken).isNotNull
        Assertions.assertThat(result.refreshToken).isNotNull
    }




    @Test
    @DisplayName("Access 토큰이 만료된 경우, Refresh 토큰도 만료되었을 때 - 403")
    fun `Access 토큰이 만료된 경우, Refresh 토큰도 만료되었을 때 - 403`() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken(refreshTokenExpirationPeriodDay = -1))

         mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isForbidden)
    }







    @Test
    @DisplayName("Access 토큰이 만료된 경우 - (AccessToken 정상, RefreshToken이 회원의 것과 다를 때 ) - 403")
    fun `Access 토큰이 만료된 경우 - (AccessToken 정상, RefreshToken이 회원의 것과 다를 때 ) - 403`() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        every { memberRepository.findByAccessTokenAndRefreshToken(any(), any()) } returns null
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken())

        mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isForbidden)
    }


    @Test
    @DisplayName("Access 토큰이 만료된 경우 - (AccessToken, RefreshToken 모두 회원의 것과 다를 때 ) - 403")
    fun `Access 토큰이 만료된 경우 - (AccessToken, RefreshToken 모두 회원의 것과 다를 때 ) - 403`() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        every { memberRepository.findByAccessTokenAndRefreshToken(any(), any()) } returns null
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken())

        mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isForbidden)
    }




    @Test
    @DisplayName("Access 토큰이 만료되지 않은 경우 - Userdetail이 정상인 경우 -> 인증 성공 (404)")
    fun `Access 토큰이 만료되지 않은 경우 - Userdetail이 정상인 경우 - 인증 성공 (404)`() {
        //given
        every { memberRepository.findByEmail(any()) } returns member()
        every { memberRepository.findByAccessTokenAndRefreshToken(any(), any()) } returns member()
        val tokenDto = createTokenDto(accessToken(),  refreshToken())

             mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isNotFound)

    }


}
