package com.whyrano.domain.post.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.fixture.PostFixture
import com.whyrano.domain.post.fixture.PostFixture.createPostRequest
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.post.service.PostService
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.exception.ExceptionController
import com.whyrano.global.exception.ExceptionResponse
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
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


    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var postService: PostService

    @MockkBean
    private lateinit var jwtService: JwtService

    @Autowired
    private lateinit var wac: WebApplicationContext





    @BeforeEach
    fun setUp() {

        //Security 필터 사용 안함
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .defaultResponseCharacterEncoding<DefaultMockMvcBuilder>(StandardCharsets.UTF_8).build()


        val authMember = MemberFixture.authMember()
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(authMember, null, authMember.authorities)
        SecurityContextHolder.setContext(context)
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
        verify(exactly = 1) { postService.create(any(), cpr.toServiceDto()) }
    }





    //== 필드 체크 ==//
    @Test
    fun `포스트 생성 실패 - 게시물 타입이 없는 경유`() {

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
                .content(jsonFormat.format("", "content", "title"))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(ExceptionController.BIND_EXCEPTION_ERROR_CODE)
        verify(exactly = 0) { postService.create(any(), any()) }
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
        verify(exactly = 0) { postService.create(any(), any()) }
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
        verify(exactly = 0) { postService.create(any(), any()) }
    }





    //== 서비스단의 예외 발생 ==//
    @Test
    fun `포스트 생성 실패 - 회원이 없는 경우`() {

        //given
        val cpr = createPostRequest()

        every { postService.create(any(), cpr.toServiceDto()) } throws MemberException(MemberExceptionType.NOT_FOUND)

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cpr))
        )
            .andExpect(status().isNotFound)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(MemberExceptionType.NOT_FOUND.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(MemberExceptionType.NOT_FOUND.message())
        verify(exactly = 1) { postService.create(any(), any()) }
    }





    @Test
    fun `포스트 생성 실패 - 질문을 올릴 권한이 없는 경우`() {

        //given
        val cpr = createPostRequest()

        every {
            postService.create(
                any(),
                cpr.toServiceDto()
            )
        } throws PostException(PostExceptionType.NO_AUTHORITY_CREATE_QUESTION)

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cpr))
        )
            .andExpect(status().isForbidden)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_QUESTION.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_QUESTION.message())
        verify(exactly = 1) { postService.create(any(), any()) }
    }





    @Test
    fun `포스트 생성 실패 - 공지를 올릴 권한이 없는 경우`() {

        //given
        val cpr = createPostRequest()

        every {
            postService.create(
                any(),
                cpr.toServiceDto()
            )
        } throws PostException(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cpr))
        )
            .andExpect(status().isForbidden)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE.message())
        verify(exactly = 1) { postService.create(any(), any()) }
    }





    @Test
    fun `포스트 수정 성공`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) } just runs

        //when
        val result = mockMvc.perform(
            put("/post/{postId}", postId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upr))
        )
            .andExpect(status().isOk)
            .andReturn()

        //then
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) }
    }





    @Test
    fun `포스트 수정 실패 - 회원이 없는 경우`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) } throws MemberException(MemberExceptionType.NOT_FOUND)

        //when
        val result = mockMvc.perform(
            put("/post/{postId}", postId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upr))
        )
            .andExpect(status().isNotFound)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(MemberExceptionType.NOT_FOUND.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(MemberExceptionType.NOT_FOUND.message())
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) }
    }





    @Test
    fun `포스트 수정 실패 - 권한이 없는 경우`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) } throws PostException(PostExceptionType.NO_AUTHORITY_UPDATE_POST)

        //when
        val result = mockMvc.perform(
            put("/post/{postId}", postId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upr))
        )
            .andExpect(status().isForbidden)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST.message())
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) }
    }





    @Test
    fun `포스트 수정 실패 - 포스트가 없는 경우`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) } throws PostException(PostExceptionType.NOT_FOUND)

        //when
        val result = mockMvc.perform(
            put("/post/{postId}", postId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upr))
        )
            .andExpect(status().isNotFound)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(PostExceptionType.NOT_FOUND.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(PostExceptionType.NOT_FOUND.message())
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId , upd = upr.toServiceDto()) }
    }





    @Test
    fun `포스트 삭제 성공`() {

        //given
        val postId = 11L

        every { postService.delete(writerId = any(), postId = postId ) } just Runs

        //when
        val result = mockMvc.perform(
            delete("/post/{postId}", postId)
        )
            .andExpect(status().isNoContent)
            .andReturn()

        //then
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId ) }
    }





    @Test
    fun `포스트 삭제 실패 - 회원이 없는 경우`() {

        //given
        val postId = 11L

        every { postService.delete(writerId = any(), postId = postId ) } throws MemberException(MemberExceptionType.NOT_FOUND)

        //when
        val result = mockMvc.perform(
            delete("/post/{postId}", postId)
        )
            .andExpect(status().isNotFound)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(MemberExceptionType.NOT_FOUND.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(MemberExceptionType.NOT_FOUND.message())
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId ) }
    }





    @Test
    fun `포스트 삭제 실패 - 권한이 없는 경우`() {

        //given
        val postId = 11L

        every { postService.delete(writerId = any(), postId = postId ) } throws PostException(PostExceptionType.NO_AUTHORITY_DELETE_POST)

        //when
        val result = mockMvc.perform(
            delete("/post/{postId}", postId)
        )
            .andExpect(status().isForbidden)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST.message())
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId ) }
    }





    @Test
    fun `포스트 삭제 실패 - 포스트가 없는 경우`() {

        //given
        val postId = 11L

        every { postService.delete(writerId = any(), postId = postId ) } throws PostException(PostExceptionType.NOT_FOUND)

        //when
        val result = mockMvc.perform(
            delete("/post/{postId}", postId)
        )
            .andExpect(status().isNotFound)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(PostExceptionType.NOT_FOUND.errorCode())
        assertThat(exceptionResponse.message).isEqualTo(PostExceptionType.NOT_FOUND.message())
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId ) }
    }





    @Test
    fun `포스트 검색 성공`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지는 1부터 시작`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지가 없는 경우 1페이지 조회`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지가 올바르지 않은 경우(숫자가 아닌 경우 1페이지 조회)`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지가 올바르지 않은 경우(음수인 경우 1페이지 조회)`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지 크기가 없는 경우 기본페이지 크기 적용`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지 크기가 올바르지 않은 경우 기본페이지 크기 적용`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 페이지 크기가 0 이하인 경우 기본페이지 크기 적용`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 정렬 필드가 올바르지 않은 경우 - 무시`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 정렬 필드는 올바르나 방향(DESC, ASC)이 올바르지 않은 경우 - 무시`() {

        //given

        //when

        //then
    }

    @Test
    fun `포스트 검색 성공 - 정렬 필드가 없는 경우 - 최근 생성일 순만 적용`() {

        //given

        //when

        //then
    }
}