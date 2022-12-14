package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.controller.MemberController
import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture.ID
import com.whyrano.domain.member.fixture.MemberFixture.accessToken
import com.whyrano.domain.member.fixture.MemberFixture.authMember
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.fixture.MemberFixture.refreshToken
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.domain.post.controller.PostController
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_NAME
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_PREFIX
import com.whyrano.global.auth.jwt.JwtService.Companion.REFRESH_TOKEN_HEADER_NAME
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.SecurityConfig
import io.mockk.every
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.security.core.userdetails.User
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ShinD on 2022/08/11.
 */
@WebMvcTest(
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [PostController::class, MemberController::class])]
)
@Import(SecurityConfig::class)
@MockkBean(MemberService::class, MemberRepository::class)
internal class JwtAuthenticationFilterTest {


    companion object {
        private val objectMapper = ObjectMapper()
        private val userDetails = User.builder().username("USERNAME").password("PASSWORD").roles(Role.BASIC.name).build()
    }


    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var jwtService: JwtService




    @Test
    @DisplayName("permitAll ?????? url??? ?????? ?????? X")
    fun test_permitAll_no_filtering() {
        mockMvc
            .perform(get("/h2-console"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("????????? ???????????? ?????? ?????? 401")
    fun test_no_token_403() {
        //given
        every { jwtService.extractToken(any()) } returns null // AccessToken??? Bearer??? ??? ????????? null??? ???????????? ?????? JwtServiceTest?????? ??????

        mockMvc
            .perform(get("/test"))
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("Access ?????? ????????? ???????????? ?????? 401")
    fun test_only_access_token_403() {
        //given
        every { jwtService.extractToken(any()) } returns null // AccessToken??? ????????? ???????????? null??? ???????????? ?????? JwtServiceTest?????? ??????
        val tokenDto = createTokenDto(accessToken())


        mockMvc
            .perform(get("/test")
                .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
            )
            .andExpect(status().isUnauthorized)
    }


    private fun createTokenDto(accessToken: AccessToken? = null, refreshToken: RefreshToken? = null): TokenDto
        = TokenDto(accessToken = accessToken?.accessToken, refreshToken =refreshToken?.refreshToken)


    @Test
    @DisplayName("Refresh ?????? ????????? ???????????? ?????? 401")
    fun test_only_refresh_token_403() {
        //given
        every { jwtService.extractToken(any()) } returns null // AccessToken??? Bearer??? ??? ????????? null??? ???????????? ?????? JwtServiceTest?????? ??????
        val tokenDto = createTokenDto(refreshToken = refreshToken())


        mockMvc
            .perform(get("/test")
                .header(REFRESH_TOKEN_HEADER_NAME,   tokenDto.refreshToken)
            )
            .andExpect(status().isUnauthorized)
    }




    @Test
    @DisplayName("Access ????????? Bearer??? ??? ?????? ?????? - (AccessToken,RefreshToken ?????? ?????? ) - 401")
    fun `Access ????????? Bearer??? ??? ?????? ?????? - (AccessToken,RefreshToken ?????? ?????? ) - 401`() {
        //given
        val tokenDto = createTokenDto(accessToken(), refreshToken())
        every { jwtService.extractToken(any()) } returns null // AccessToken??? Bearer??? ??? ????????? null??? ???????????? ?????? JwtServiceTest?????? ??????


        mockMvc
            .perform(get("/test")
                .header(ACCESS_TOKEN_HEADER_NAME,  tokenDto.accessToken)
                .header(REFRESH_TOKEN_HEADER_NAME,   tokenDto.refreshToken)
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("Access ????????? ????????? ?????? - (AccessToken,RefreshToken ?????? ?????? ) - 200??? ?????? ?????????")
    fun `Access ????????? ????????? ?????? - (AccessToken,RefreshToken ?????? ?????? ) - 200??? ?????? ?????????`() {
        //given
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken())
        every { jwtService.extractToken(any()) } returns tokenDto
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid(any()) } returns true //RefreshToken??? ???????????? ??????
        every { jwtService.findMemberByTokens(any(), any()) } returns member(id = ID)
        every { jwtService.createAccessAndRefreshToken(any()) } returns tokenDto


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
    @DisplayName("Access ????????? ????????? ??????, Refresh ????????? ??????????????? ??? - 401")
    fun `Access ????????? ????????? ??????, Refresh ????????? ??????????????? ??? - 401`() {
        //given
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken(refreshTokenExpirationPeriodDay = -1))
        every { jwtService.extractToken(any()) } returns tokenDto
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid(any()) } returns false // refresh Token??? ??????

        mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isUnauthorized)
    }







    @Test
    @DisplayName("Access ????????? ????????? ?????? - (AccessToken ??????, RefreshToken??? ????????? ?????? ?????? ??? ) - 401")
    fun `Access ????????? ????????? ?????? - (AccessToken ??????, RefreshToken??? ????????? ?????? ?????? ??? ) - 401`() {
        //given
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken())
        every { jwtService.extractToken(any()) } returns tokenDto
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid(any()) } returns true
        every { jwtService.findMemberByTokens(any(), any()) } returns null

        mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("Access ????????? ????????? ?????? - (AccessToken, RefreshToken ?????? ????????? ?????? ?????? ??? ) - 401")
    fun `Access ????????? ????????? ?????? - (AccessToken, RefreshToken ?????? ????????? ?????? ?????? ??? ) - 401`() {
        //given
        val tokenDto = createTokenDto(accessToken(accessTokenExpirationPeriodDay = -1),  refreshToken())
        every { jwtService.extractToken(any()) } returns tokenDto
        every { jwtService.isValidMoreThanMinute(any(), any()) }  returns false
        every { jwtService.isValid(any()) }  returns false

        mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isUnauthorized)
    }




    @Test
    @DisplayName("Access ????????? ???????????? ?????? ?????? - Userdetail??? ????????? ?????? -> ?????? ?????? (404)")
    fun `Access ????????? ???????????? ?????? ?????? - Userdetail??? ????????? ?????? - ?????? ?????? (404)`() {
        //given
        val tokenDto = createTokenDto(accessToken(),  refreshToken())
        every { jwtService.extractToken(any()) } returns tokenDto
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(any()) } returns authMember()

        mockMvc
            .perform(
                get("/test")
                    .header(ACCESS_TOKEN_HEADER_NAME, ACCESS_TOKEN_HEADER_PREFIX + tokenDto.accessToken)
                    .header(REFRESH_TOKEN_HEADER_NAME, tokenDto.refreshToken)
            )
            .andExpect(status().isNotFound)

    }
}
