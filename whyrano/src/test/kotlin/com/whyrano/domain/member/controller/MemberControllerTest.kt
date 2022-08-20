package com.whyrano.domain.member.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.controller.request.PasswordDto
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.exception.MemberExceptionType.NOT_FOUND
import com.whyrano.domain.member.exception.MemberExceptionType.UNMATCHED_PASSWORD
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
import com.whyrano.global.exception.ExceptionController.Companion.BIND_EXCEPTION_ERROR_CODE
import com.whyrano.global.exception.ExceptionController.Companion.BIND_EXCEPTION_MESSAGE
import com.whyrano.global.exception.ExceptionResponse
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets.UTF_8


/**
 * Persistence 관련해서는 Service Test에서 진행하므로, 이곳에서는 하지 않아도 된다고 생각함
 */

@WebMvcTest(controllers = [MemberController::class])
@Import(SecurityConfig::class)
@MockkBean(JwtService::class, MemberRepository::class, MemberService::class)
internal class MemberControllerTest {

    companion object {

        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }



    @Autowired
    private lateinit var mockMvc: MockMvc



    @MockkBean
    private lateinit var memberService: MemberService



    @MockkBean
    private lateinit var jwtService: JwtService



    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(mockMvc, "defaultResponseCharacterEncoding", UTF_8)
    }



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
        verify(exactly = 1) { memberService.signUp(any()) }
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
        verify(exactly = 1) { memberService.signUp(any()) }
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
        verify(exactly = 1) { memberService.signUp(any()) }
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


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(BIND_EXCEPTION_ERROR_CODE)
        assertThat(readValue.message).isEqualTo(BIND_EXCEPTION_MESSAGE)
        verify(exactly = 0) { memberService.signUp(any()) }
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


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(BIND_EXCEPTION_ERROR_CODE)
        assertThat(readValue.message).isEqualTo(BIND_EXCEPTION_MESSAGE)
        assertThat(result.response.getHeader("location")).isNull()
        verify(exactly = 0) { memberService.signUp(any()) }
    }



    @Test
    fun `회원가입 실패 - nickname이 없는 경우`() {
        //given
        val createMemberId = 11L
        val cmr = createMemberRequest(nickname = "       ")
        every { memberService.signUp(cmr.toServiceDto()) } returns createMemberId


        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(BIND_EXCEPTION_ERROR_CODE)
        assertThat(readValue.message).isEqualTo(BIND_EXCEPTION_MESSAGE)
        assertThat(result.response.getHeader("location")).isNull()
        verify(exactly = 0) { memberService.signUp(any()) }
    }



    @Test
    fun `회원가입 실패 - 이메일이 중복인 경우`() {
        //given
        val cmr = createMemberRequest()
        every { memberService.signUp(cmr.toServiceDto()) } throws MemberException(MemberExceptionType.ALREADY_EXIST)


        val result = mockMvc.perform(
            post("/signup")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cmr))
        )
            .andExpect(status().isConflict)
            .andReturn()


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(MemberExceptionType.ALREADY_EXIST.errorCode())
        assertThat(readValue.message).isEqualTo(MemberExceptionType.ALREADY_EXIST.message())
        assertThat(result.response.getHeader("location")).isNull()
        verify(exactly = 1) { memberService.signUp(any()) }
    }



    @Test
    fun `회원수정 성공`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } just runs
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(any()) } returns authMember(id = memberId)




        mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isOk)




        verify(exactly = 1) { memberService.update(any(), umr.toServiceDto()) }
    }



    @Test
    fun `회원수정 실패 - 없는 회원인 경우`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } throws MemberException(MemberExceptionType.NOT_FOUND)
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(any()) } returns authMember(id = memberId)


        val result = mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isNotFound)
            .andReturn()


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(MemberExceptionType.NOT_FOUND.errorCode())
        assertThat(readValue.message).isEqualTo(MemberExceptionType.NOT_FOUND.message())
        verify(exactly = 1) { memberService.update(any(), umr.toServiceDto()) }
    }



    @Test
    fun `회원수정 실패 - AccessToken이 만료, RefreshToken은 멀쩡한 경우`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } just runs
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid(any()) } returns true
        every { jwtService.findMemberByTokens(any(), any()) } returns member(id = memberId)
        every { jwtService.createAccessAndRefreshToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )




        mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isOk)




        verify(exactly = 0) { memberService.update(any(), umr.toServiceDto()) }
    }



    @Test
    fun `회원수정 실패 - AccessToken이 만료, RefreshToken도 만료된 경우`() {
        //given
        val umr = MemberFixture.updateMemberRequest()
        val memberId = 10L
        every { memberService.update(any(), umr.toServiceDto()) } just runs
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns false
        every { jwtService.isValid(any()) } returns false




        mockMvc.perform(
            put("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(umr))
        )
            .andExpect(status().isUnauthorized)




        verify(exactly = 0) { memberService.update(any(), umr.toServiceDto()) }
    }



    @Test
    fun `회원탈퇴 성공`() {
        //given
        val memberId = 10L
        val password = "example"
        val accessToken = accessToken(id = memberId)
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken.accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(accessToken) } returns authMember(id = memberId)
        every { memberService.delete(memberId, password) } just runs





        mockMvc.perform(
            delete("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PasswordDto(password)))
        )
            .andExpect(status().isNoContent)





        verify(exactly = 1) { memberService.delete(memberId, password) }
    }



    @Test
    fun `회원탈퇴 실패 - 회원이 없는 경우`() {
        //given
        val memberId = 10L
        val password = "example"
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(accessToken(id = memberId)) } returns authMember(id = memberId)
        every { memberService.delete(memberId, password) } throws MemberException(NOT_FOUND)


        val result = mockMvc.perform(
            delete("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PasswordDto(password)))
        )
            .andExpect(status().isNotFound)
            .andReturn()


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(NOT_FOUND.errorCode())
        assertThat(readValue.message).isEqualTo(NOT_FOUND.message())
        verify(exactly = 1) { memberService.delete(memberId, password) }
    }



    @Test
    fun `회원탈퇴 실패 - 비밀번호가 없는 경우`() {
        //given
        val memberId = 10L
        val password = "example"
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(accessToken(id = memberId)) } returns authMember(id = memberId)
        every { memberService.delete(memberId, password) } just runs


        val result = mockMvc.perform(
            delete("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PasswordDto("")))
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(BIND_EXCEPTION_ERROR_CODE)
        assertThat(readValue.message).isEqualTo(BIND_EXCEPTION_MESSAGE)
        verify(exactly = 0) { memberService.delete(memberId, password) }
    }



    @Test
    fun `회원탈퇴 실패 - 비밀번호가 다른 경우 - 비밀번호가 다르다는 예외`() {
        //given
        val memberId = 10L
        val password = "example"
        every { jwtService.extractToken(any()) } returns TokenDto(
            accessToken(id = memberId).accessToken,
            refreshToken().refreshToken
        )
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(accessToken(id = memberId)) } returns authMember(id = memberId)
        every { memberService.delete(memberId, password) } throws MemberException(UNMATCHED_PASSWORD)


        val result = mockMvc.perform(
            delete("/member")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(PasswordDto(password)))
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(UNMATCHED_PASSWORD.errorCode())
        verify(exactly = 1) { memberService.delete(memberId, password) }
    }
}