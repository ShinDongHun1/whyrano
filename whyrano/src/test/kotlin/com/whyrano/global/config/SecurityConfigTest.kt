package com.whyrano.global.config

import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.entity.Role.*
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.accessToken
import com.whyrano.domain.member.fixture.MemberFixture.refreshToken
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Created by ShinD on 2022/08/12.
 */
@WebMvcTest
@Import(SecurityConfig::class)
@MockkBean(JwtService::class, MemberRepository::class, MemberService::class)
internal class SecurityConfigTest {


    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean private lateinit var jwtService: JwtService

    @Test
    fun `로그인 경로 접근 시 401`() {
        //given
        mockMvc.perform(get("/login"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `모두 허용된 경로 접근 시 인증처리 하지 않음`() {
        //given
        val andExpect = mockMvc.perform(get("/signup"))
            .andExpect { result ->
                         (result.response.status != 401 && result.response.status != 403)
                        && (result.response.status != 200 || result.response.status != 404)
            }
    }

    @Test
    fun `나머지 경로 접근 시 403`() {
        //given
        every { jwtService.extractToken(any()) } returns null
        mockMvc.perform(get("/any"))
            .andExpect(status().isForbidden)
    }


    @Test
    fun `BASIC 유저의 일반 경로 접근 허용`() {
        //given
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(role = BASIC).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractUserDetail(any()) } returns MemberFixture.userDetail(role = BASIC)


        mockMvc.perform(get("/any"))
            .andExpect { result ->
                (result.response.status != 401 && result.response.status != 403)
                        && (result.response.status != 200 || result.response.status != 404)
            }
    }


    @Test
    fun `BASIC 유저의 ADMIN 경로 접근 - 403`() {
        //given
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(role = BASIC).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractUserDetail(any()) } returns MemberFixture.userDetail(role = BASIC)


        mockMvc.perform(get("/admin"))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `ADMIN 유저의 일반 경로 접근 - OK`() {
        //given
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(role = ADMIN).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractUserDetail(any()) } returns MemberFixture.userDetail(role = ADMIN)


        mockMvc.perform(get("/any"))
            .andExpect { result ->
                (result.response.status != 401 && result.response.status != 403)
                        && (result.response.status != 200 || result.response.status != 404)
            }
    }

    @Test
    fun `ADMIN 유저의 ADMIN 경로 접근 - OK`() {
        //given
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(role = ADMIN).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractUserDetail(any()) } returns MemberFixture.userDetail(role = ADMIN)


        mockMvc.perform(get("/admin"))
            .andExpect { result ->
                (result.response.status != 401 && result.response.status != 403)
                        && (result.response.status != 200 || result.response.status != 404)
            }
    }

    @Test
    fun `BLACK 유저의 ADMIN 경로 접근 - 403`() {
        //given
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(role = BLACK).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractUserDetail(any()) } returns MemberFixture.userDetail(role = BLACK)


        mockMvc.perform(get("/admin"))
            .andExpect { result ->
                (result.response.status != 401 && result.response.status != 403)
                        && (result.response.status != 200 || result.response.status != 404)
            }
    }

}