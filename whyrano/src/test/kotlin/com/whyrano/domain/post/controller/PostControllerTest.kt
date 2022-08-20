package com.whyrano.domain.post.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.common.search.SearchResultDto
import com.whyrano.domain.common.search.SearchResultResponse
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.domain.post.controller.response.SimplePostResponse
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.fixture.PostFixture
import com.whyrano.domain.post.fixture.PostFixture.createPostRequest
import com.whyrano.domain.post.fixture.PostFixture.postSearchCond
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.post.service.PostService
import com.whyrano.domain.post.service.dto.SimplePostDto
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.exception.ExceptionController
import com.whyrano.global.exception.ExceptionController.Companion.BIND_EXCEPTION_ERROR_CODE
import com.whyrano.global.exception.ExceptionController.Companion.BIND_EXCEPTION_MESSAGE
import com.whyrano.global.exception.ExceptionResponse
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
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



    @Autowired
    private lateinit var springDataWebProperties: SpringDataWebProperties



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



    @Test
    fun `포스트 생성 성공 - 잘못된 타입이 존재하나, 필요한 모든 필드는 정상인 경우`() {

        //given
        val createPostId = 11L
        every { postService.create(any(), any()) } returns createPostId
        val jsonFormat = """
            {
                "postType":"%s",
                "content":"%s",
                "title":"%s",
                "tags":[],
                "titleddd":"%s"
            }
        """

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(jsonFormat.format("NOTICE", "content", "title", "titleddd"))
        )
            .andExpect(status().isCreated)
            .andReturn()

        //then
        assertThat(result.response.getHeader("location")).contains("/post/${createPostId}")
        verify(exactly = 1) { postService.create(any(), any()) }
    }



    //== 필드 체크 ==//
    @Test
    fun `포스트 생성 실패 - 게시물 타입이 없는 경우`() {

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
    fun `포스트 생성 실패 - 게시물 타입이 잘못된 경우 - 예외 발생`() {

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
                .content(jsonFormat.format("NOTYPE", "content", "title"))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(ExceptionController.BIND_EXCEPTION_ERROR_CODE)
        verify(exactly = 0) { postService.create(any(), any()) }
    }



    @Test
    fun `포스트 생성 실패 - 잘못된 타입이 존재하는 경우(title 대신 titleddd) - 예외 발생`() {

        //given
        val jsonFormat = """
            {
                "postType":"%s",
                "content":"%s",
                "titleddd":"%s"
            }
        """

        //when
        val result = mockMvc.perform(
            post("/post")
                .contentType(APPLICATION_JSON)
                .content(jsonFormat.format("NOTICE", "content", "title"))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        //then
        val exceptionResponse = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(exceptionResponse.errorCode).isEqualTo(ExceptionController.BIND_EXCEPTION_ERROR_CODE)
        verify(exactly = 0) { postService.create(any(), any()) }
    }



    @Test
    fun `포스트 생성 실패 - 제목이 없는 경우`() {

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
    fun `포스트 생성 실패 - 내용이 없는 경우`() {

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

        every { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) } just runs

        //when
        val result = mockMvc.perform(
            put("/post/{postId}", postId)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(upr))
        )
            .andExpect(status().isOk)
            .andReturn()

        //then
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) }
    }



    @Test
    fun `포스트 수정 실패 - 회원이 없는 경우`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every {
            postService.update(
                writerId = any(),
                postId = postId,
                upd = upr.toServiceDto()
            )
        } throws MemberException(MemberExceptionType.NOT_FOUND)

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
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) }
    }



    @Test
    fun `포스트 수정 실패 - 권한이 없는 경우`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) } throws PostException(
            PostExceptionType.NO_AUTHORITY_UPDATE_POST
        )

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
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) }
    }



    @Test
    fun `포스트 수정 실패 - 포스트가 없는 경우`() {

        //given
        val postId = 11L
        val upr = PostFixture.updatePostRequest()

        every { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) } throws PostException(
            PostExceptionType.NOT_FOUND
        )

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
        verify(exactly = 1) { postService.update(writerId = any(), postId = postId, upd = upr.toServiceDto()) }
    }



    @Test
    fun `포스트 삭제 성공`() {

        //given
        val postId = 11L

        every { postService.delete(writerId = any(), postId = postId) } just Runs

        //when
        val result = mockMvc.perform(
            delete("/post/{postId}", postId)
        )
            .andExpect(status().isNoContent)
            .andReturn()

        //then
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId) }
    }



    @Test
    fun `포스트 삭제 실패 - 회원이 없는 경우`() {

        //given
        val postId = 11L

        every {
            postService.delete(
                writerId = any(),
                postId = postId
            )
        } throws MemberException(MemberExceptionType.NOT_FOUND)

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
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId) }
    }



    @Test
    fun `포스트 삭제 실패 - 권한이 없는 경우`() {

        //given
        val postId = 11L

        every {
            postService.delete(
                writerId = any(),
                postId = postId
            )
        } throws PostException(PostExceptionType.NO_AUTHORITY_DELETE_POST)

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
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId) }
    }



    @Test
    fun `포스트 삭제 실패 - 포스트가 없는 경우`() {

        //given
        val postId = 11L

        every {
            postService.delete(
                writerId = any(),
                postId = postId
            )
        } throws PostException(PostExceptionType.NOT_FOUND)

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
        verify(exactly = 1) { postService.delete(writerId = any(), postId = postId) }
    }



    @Test
    fun `포스트 검색 성공`() {

        //given
        val pageParam = 10
        val pageSizeParam = 5
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(pageParam - 1, pageSizeParam, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. pageSizeParam) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 30
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam - 1, //페이지는 1부터 시작인데, Service에 넘길때는 1을 빼서 넘기므로, 반환되는 값도 1을 빼야이어야 함
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("page", pageParam.toString())
                .param("size", pageSizeParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()


        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam, // 1을 감소시켜 서비스에 전달하므로, 반환은 1을 증가시켜야 함
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    fun `포스트 검색 성공 - 페이지가 없는 경우 1페이지 조회`() {

        //given
        val pageSizeParam = 5
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(0, pageSizeParam, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. pageSizeParam) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 30
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = 0,
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("size", pageSizeParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()

        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = 1,
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    fun `포스트 검색 성공 - 페이지가 올바르지 않은 경우(숫자가 아닌 경우 1페이지 조회)`() {

        //given
        val pageSizeParam = 5
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(0, pageSizeParam, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. pageSizeParam) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 30
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = 0,
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("page", "non number")
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("size", pageSizeParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()

        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = 1,
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    fun `포스트 검색 성공 - 페이지가 올바르지 않은 경우(음수인 경우 1페이지 조회)`() {

        //given
        val pageSizeParam = 5
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(0, pageSizeParam, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. pageSizeParam) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 30
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = 0,
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("page", "-1")
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("size", pageSizeParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()

        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = 1,
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    fun `포스트 검색 성공 - 페이지 크기가 없는 경우 기본페이지 크기 적용`() {

        //given
        val pageParam = 10
        val defaultPageSize = springDataWebProperties.pageable.defaultPageSize
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(pageParam - 1, defaultPageSize, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. defaultPageSize) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 20
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam - 1, //페이지는 1부터 시작인데, Service에 넘길때는 1을 빼서 넘기므로, 반환되는 값도 1을 빼야이어야 함
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("page", pageParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()


        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam, // 1을 감소시켜 서비스에 전달하므로, 반환은 1을 증가시켜야 함
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    fun `포스트 검색 성공 - 페이지 크기가 올바르지 않은 경우(문자) 기본페이지 크기 적용`() {

        //given
        val pageParam = 10
        val defaultPageSize = springDataWebProperties.pageable.defaultPageSize
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(pageParam - 1, defaultPageSize, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. defaultPageSize) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 20
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam - 1, //페이지는 1부터 시작인데, Service에 넘길때는 1을 빼서 넘기므로, 반환되는 값도 1을 빼야이어야 함
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("pageSize", "string")
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("page", pageParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()


        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam, // 1을 감소시켜 서비스에 전달하므로, 반환은 1을 증가시켜야 함
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    fun `포스트 검색 성공 - 페이지 크기가 0 이하인 경우 기본페이지 크기 적용`() {

        //given
        val pageParam = 10
        val defaultPageSize = springDataWebProperties.pageable.defaultPageSize
        val postTypeParam = "NOTICE"
        val sortCreatedDateParam = ASC

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(pageParam - 1, defaultPageSize, Sort.by(Sort.Order(ASC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. defaultPageSize) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 20
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam - 1, //페이지는 1부터 시작인데, Service에 넘길때는 1을 빼서 넘기므로, 반환되는 값도 1을 빼야이어야 함
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("pageSize", "-1")
                .param("sort", "createdDate,${sortCreatedDateParam}")
                .param("page", pageParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()


        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam, // 1을 감소시켜 서비스에 전달하므로, 반환은 1을 증가시켜야 함
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }



    @Test
    @DisplayName("포스트 검색 성공 - 정렬 조건이 없는 경우 기본값 (createdDate, DESC) 적용")
    fun `포스트 검색 성공 - 정렬 조건이 없는 경우 기본값 (createdDate, DESC) 적용`() {

        //given
        val pageParam = 10
        val defaultPageSize = springDataWebProperties.pageable.defaultPageSize
        val pageable = PageRequest.of(pageParam - 1, defaultPageSize, Sort.by(Sort.Order(DESC, "createdDate")))

        val list = mutableListOf<SimplePostDto>()
        for (i in 0 .. defaultPageSize) {
            list.add(PostFixture.simplePostDto())
        }


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 20
        every { postService.search(any(), pageable = pageable) } returns SearchResultDto<SimplePostDto>(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam - 1, //페이지는 1부터 시작인데, Service에 넘길때는 1을 빼서 넘기므로, 반환되는 값도 1을 빼야이어야 함
            currentElementCount = currentElementCount,
            simpleDtos = list
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("page", pageParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()


        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(SearchResultResponse(totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam, // 1을 감소시켜 서비스에 전달하므로, 반환은 1을 증가시켜야 함
            currentElementCount = currentElementCount,
            simpleDataResponses = list.map { SimplePostResponse.from(it) }

        ))

        verify(exactly = 1) { postService.search(postSearchCond = any(), pageable = pageable) }
    }



    @Test
    @DisplayName("포스트 검색 성공 - 검색하는 Post Type이 올바르지 않은 값이 들어오는 경우 - Binding 예외 발생")
    fun `포스트 검색 성공 - 검색하는 Post Type이 올바르지 않은 값이 들어오는 경우 - Binding 예외 발생`() {

        //given
        val postTypeParam = "STRANGE"

        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
        )
            .andExpect(status().isBadRequest)
            .andReturn()


        //then
        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)

        assertThat(readValue.errorCode).isEqualTo(BIND_EXCEPTION_ERROR_CODE)
        assertThat(readValue.message).isEqualTo(BIND_EXCEPTION_MESSAGE)

        verify(exactly = 0) { postService.search(any(), any()) }
    }



    @Test
    @DisplayName("포스트 검색 성공 - 정렬 필드는 올바르나 ASC, DESC가 아닌 aSEC 등과 같이 잘못된 경우 - 정렬 필드는 기본값인 ASC로 초기화")
    fun `포스트 검색 성공 - 정렬 필드는 올바르나 ASC, DESC가 아닌 aSEC 등과 같이 잘못된 경우 - 정렬 필드는 기본값인 ASC로 초기화`() {

        //given
        //given
        val pageParam = 10
        val defaultPageSize = springDataWebProperties.pageable.defaultPageSize
        val postTypeParam = "NOTICE"
        val strangeDirection = "aSEC"

        val postSearchCond = postSearchCond(postType = PostType.valueOf(postTypeParam))
        val pageable = PageRequest.of(
            pageParam - 1,
            defaultPageSize,
            Sort.by(Sort.Order(ASC, "createdDate"), Sort.Order(ASC, strangeDirection))
        )


        val totalPage = 10
        val totalElementCount = 300L
        val currentElementCount = 20
        every {
            postService.search(
                postSearchCond = postSearchCond,
                pageable = pageable
            )
        } returns SearchResultDto(
            totalPage = totalPage,
            totalElementCount = totalElementCount,
            currentPage = pageParam - 1, //페이지는 1부터 시작인데, Service에 넘길때는 1을 빼서 넘기므로, 반환되는 값도 1을 빼야이어야 함
            currentElementCount = currentElementCount,
            simpleDtos = emptyList()
        )


        //when
        val result = mockMvc.perform(
            get("/post")
                .param("postType", postTypeParam)
                .param("pageSize", "-1")
                .param("sort", "createdDate,${strangeDirection}")
                .param("page", pageParam.toString())
        )
            .andExpect(status().isOk)
            .andReturn()


        //then
        val typeRef = object : TypeReference<SearchResultResponse<SimplePostResponse>>() {}
        val readValue = objectMapper.readValue(result.response.contentAsString, typeRef)

        assertThat(readValue).isEqualTo(
            SearchResultResponse(
                totalPage = totalPage,
                totalElementCount = totalElementCount,
                currentPage = pageParam, // 1을 감소시켜 서비스에 전달하므로, 반환은 1을 증가시켜야 함
                currentElementCount = currentElementCount,
                simpleDataResponses = emptyList<SimplePostDto>()

            )
        )

        verify(exactly = 1) { postService.search(postSearchCond = postSearchCond, pageable = pageable) }
    }
}