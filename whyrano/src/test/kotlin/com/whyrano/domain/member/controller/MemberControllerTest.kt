package com.whyrano.domain.member.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.fixture.MemberFixture.createMemberRequest
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.config.SecurityConfig
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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





}