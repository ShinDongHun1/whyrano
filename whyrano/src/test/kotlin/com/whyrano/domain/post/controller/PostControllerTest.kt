package com.whyrano.domain.post.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.domain.post.fixture.PostFixture.createPostRequest
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.post.service.PostService
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.exception.ExceptionController
import com.whyrano.global.exception.ExceptionResponse
import io.mockk.every
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets

/**
 * Created by ShinD on 2022/08/16.
 */
@WebMvcTest(controllers = [PostController::class])
@Import(SecurityConfig::class)
@MockkBean(JwtService::class, MemberRepository::class, MemberService::class, PostRepository::class, PostService::class)
internal class PostControllerTest {

    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var postService: PostService

    @MockkBean
    private lateinit var jwtService: JwtService


    @BeforeEach
    fun setUp() {
        ReflectionTestUtils.setField(mockMvc, "defaultResponseCharacterEncoding", StandardCharsets.UTF_8)

        every { jwtService.extractToken(any()) } returns TokenDto(MemberFixture.accessToken(id = 1L).accessToken, MemberFixture.refreshToken().refreshToken)
        every { jwtService.isValidMoreThanMinute(any(), any()) } returns true
        every { jwtService.extractAuthMember(any()) } returns MemberFixture.authMember(id = 1L)
    }


    @Test
    fun `포스트 생성 성공`() {
        //given
        val createPostId = 11L
        val cpr = createPostRequest()

        every { postService.create(any(), cpr.toServiceDto()) } returns createPostId

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cpr))
        )
            .andExpect(status().isCreated)
            .andReturn()

        //then
        assertThat(result.response.getHeader("location")).contains("/post/${createPostId}")
        verify (exactly = 1) { postService.create(any(), cpr.toServiceDto()) }
    }





    @Test
    fun `포스트 생성 실패 - post 타입이 없는 경유`() {

        //given
        val jsonFormat = """
            {
                "postType":"%s",
                "content":"%s",
                "title":"%s"
            }
        """

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(jsonFormat.format("","content","title"))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(ExceptionController.BIND_EXCEPTION_ERROR_CODE)
        verify (exactly = 0) { postService.create(any(), any()) }
    }




    @Test
    fun `포스트 생성 실패 - 제목이 없는 경유`() {

        //given
        val createPostId = 11L
        val cpr = createPostRequest(title = "")

        every { postService.create(any(), cpr.toServiceDto()) } returns createPostId

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cpr))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(ExceptionController.BIND_EXCEPTION_ERROR_CODE)
        verify (exactly = 0) { postService.create(any(), any()) }
    }
    @Test
    fun `포스트 생성 실패 - 내용이 없는 경유`() {

        //given
        val createPostId = 11L
        val cpr = createPostRequest(content = "          ")

        every { postService.create(any(), cpr.toServiceDto()) } returns createPostId

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cpr))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(ExceptionController.BIND_EXCEPTION_ERROR_CODE)
        verify (exactly = 0) { postService.create(any(), any()) }
    }
}