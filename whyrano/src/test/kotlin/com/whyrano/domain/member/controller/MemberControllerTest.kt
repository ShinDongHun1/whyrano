package com.whyrano.domain.member.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.accessToken
import com.whyrano.domain.member.fixture.MemberFixture.authMember
import com.whyrano.domain.member.fixture.MemberFixture.createMemberRequest
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.fixture.MemberFixture.refreshToken
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.SecurityConfig
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Persistence 관련해서는 Service Test에서 진행하므로, 이곳에서는 하지 않아도 된다고 생각함
 */

@WebMvcTest(controllers = [MemberController::class])
@Import(SecurityConfig::class)
@MockkBean(JwtService::class, MemberRepository::class, MemberService::class)
internal class MemberControllerTest {
    /**
     * 회원가입
     *
     * 회원수정
     *
     * 회원삭제
     */
    companion object {
        private val objectMapper = ObjectMapper()
    }
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberService: MemberService
    @MockkBean
    private lateinit var jwtService: JwtService


    @Test
    fun `회원가입 성공`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest()

        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId

        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
            )
            .andExpect(status().isCreated)
            .andReturn()


        assertThat(result.response.getHeader("location")).contains("/member/${createMemberId}")

        verify (exactly = 1) { memberService.signUp(any()) }
    }



    @Test
    fun `회원가입 성공 - profile 이미지 경로가 ""인 경우`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest(profileImagePath = "")

        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId

        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isCreated)
            .andReturn()


        assertThat(result.response.getHeader("location")).contains("/member/${createMemberId}")

        verify (exactly = 1) { memberService.signUp(any()) }
    }

    @Test
    fun `회원가입 성공 - profile 이미지 경로가 null인 경우`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest(profileImagePath = null)

        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId

        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isCreated)
            .andReturn()


        assertThat(result.response.getHeader("location")).contains("/member/${createMemberId}")

        verify (exactly = 1) { memberService.signUp(any()) }
    }



    @Test
    fun `회원가입 실패 - email이 없는 경우`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest(email = "")

        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId

        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        assertThat(result.response.getHeader("location")).isNull()

        verify (exactly = 0){ memberService.signUp(any()) }
    }



    @Test
    fun `회원가입 실패 - password가 없는 경우`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest(password = "")

        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId

        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        assertThat(result.response.getHeader("location")).isNull()

        verify (exactly = 0){ memberService.signUp(any()) }
    }


    @Test
    fun `회원가입 실패 - nickname이 없는 경우`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest(nickname= "")

        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId

        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        assertThat(result.response.getHeader("location")).isNull()

        verify (exactly = 0){ memberService.signUp(any()) }
    }



    //TODO(여기부터!!!)
    /**
     * 회원 수정 시 -> member로 put 요청을 보낸다
     *
     * 시큐리티 인증정보로부터 회원 정보를 받아온당
     *
     * 업데이트한다.
     */
    @Test
    fun `회원수정 성공`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } just runs
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(id = memberId).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(any()) } returns authMember(id = memberId)

        mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isOk)


        verify (exactly = 1){ memberService.update(any(), umr.toServiceDto()) }
    }




    @Test
    fun `회원수정 실패 - AccessToken이 만료, RefreshToken은 멀쩡한 경우`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } just runs
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(id = memberId).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid( any()) } returns true
        every { jwtService.findMemberByTokens( any(), any()) } returns member(id= memberId)
        every { jwtService.createAccessAndRefreshToken( any()) } returns TokenDto(accessToken(id = memberId).accessToken, refreshToken().refreshToken)

        mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isOk)


        verify (exactly = 0){ memberService.update(any(), umr.toServiceDto()) }
    }


    @Test
    fun `회원수정 실패 - AccessToken이 만료, RefreshToken도 만료된 경우`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } just runs
        every { jwtService.extractToken(any()) } returns TokenDto(accessToken(id = memberId).accessToken, refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid( any()) } returns false

        mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isForbidden)


        verify (exactly = 0){ memberService.update(any(), umr.toServiceDto()) }
    }

}