package com.whyrano.domain.post.service

import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.common.search.SearchResultDto
import com.whyrano.domain.member.entity.Role.*
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType.NOTICE
import com.whyrano.domain.post.entity.PostType.QUESTION
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.fixture.PostFixture.UPDATE_CONTENT
import com.whyrano.domain.post.fixture.PostFixture.UPDATE_TITLE
import com.whyrano.domain.post.fixture.PostFixture.createPostDto
import com.whyrano.domain.post.fixture.PostFixture.post
import com.whyrano.domain.post.fixture.PostFixture.postPageable
import com.whyrano.domain.post.fixture.PostFixture.postSearchCond
import com.whyrano.domain.post.fixture.PostFixture.updatePostDto
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.post.service.dto.SimplePostDto
import com.whyrano.domain.tag.entity.Tag
import com.whyrano.domain.tag.fixture.TagFixture
import com.whyrano.domain.tag.repository.TagRepository
import com.whyrano.domain.taggedpost.entity.TaggedPost
import com.whyrano.domain.taggedpost.fixture.TaggedPostFixture
import com.whyrano.domain.taggedpost.repository.TaggedPostRepository
import com.whyrano.global.auth.userdetails.AuthMember
import com.whyrano.global.config.JpaConfig
import com.whyrano.global.config.QuerydslConfig
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull

/**
 * Created by ShinD on 2022/08/14.
 */
@DataJpaTest
@Import(JpaConfig::class, QuerydslConfig::class)
internal class PostServiceTest {


    /**
     * 질문 작성
     * 질문 수정
     * 질문 삭제
     *
     * TODO 질문 검색
     */

    private lateinit var postService: PostService

    @MockkBean private lateinit var postRepository: PostRepository
    @MockkBean private lateinit var memberRepository: MemberRepository
    @MockkBean private lateinit var tagRepository: TagRepository
    @MockkBean private lateinit var taggedPostRepository: TaggedPostRepository

    private lateinit var basicAuthMember: AuthMember // 일반 회원
    private lateinit var adminAuthMember: AuthMember // 관리자
    private lateinit var blackAuthMember: AuthMember // 블랙리스트


    @BeforeEach
    fun setUp() {
        //PostService 세팅
        postService = PostService(memberRepository, postRepository, tagRepository, taggedPostRepository)


        // 회원들 인증정보 세팅
        basicAuthMember = MemberFixture.authMember(id = 1L, email = "basic@example.com", role = BASIC)
        adminAuthMember =MemberFixture.authMember(id = 2L,  email = "admin@example.com", role = ADMIN)
        blackAuthMember = MemberFixture.authMember(id = 3L,  email = "black@example.com", role = BLACK)


        // 회원들 DB에 저장
        every { memberRepository.findByIdOrNull(basicAuthMember.id) } returns member(id = basicAuthMember.id, authority = BASIC, email = "basic@example.com")
        every { memberRepository.findByIdOrNull(adminAuthMember.id) } returns member(id = adminAuthMember.id, authority = ADMIN, email = "admin@example.com")
        every { memberRepository.findByIdOrNull(blackAuthMember.id) } returns member(id = blackAuthMember.id, authority = BLACK, email = "black@example.com")

    }



    @Test
    fun `질문 작성 성공`() {

        //given
        val cpd = createPostDto(postType = QUESTION) // 질문 생성 DTO
        val savedPost = post()
        val savedTags = TagFixture.savedTags()
        every { postRepository.save(any()) } returns savedPost
        every { tagRepository.saveAll<Tag>(any()) } returns savedTags
        every { taggedPostRepository.saveAll<TaggedPost>(any()) } returns TaggedPostFixture.savedTaggedPosts(post = savedPost, tags = savedTags)

        //when
        val postId = postService.create(basicAuthMember.id, cpd) // 질문 작성

        //then
        verify(exactly = 1) { postRepository.save(any()) }
        verify(exactly = 1) { tagRepository.saveAll<Tag>(any()) }
        verify(exactly = 1) { taggedPostRepository.saveAll<TaggedPost>(any()) }
    }




    @Test
    fun `질문 작성 실패 - 블랙리스트인 경우`() {

        //given
        val cpd = createPostDto(postType = QUESTION) // 질문 생성 DTO

        //when
        val exceptionType =  // 블랙리스트가 질문을 작성하려는 경우
            assertThrows<PostException> { postService.create(blackAuthMember.id, cpd) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_QUESTION)

        verify (exactly = 0){ postRepository.save(any()) }
    }




    @Test
    fun `공지 작성 성공`() {

        //given
        val cpd = createPostDto(postType = NOTICE) // 질문 생성 DTO
        val savedPost = post()
        val savedTags = TagFixture.savedTags()
        every { postRepository.save(any()) } returns savedPost
        every { tagRepository.saveAll<Tag>(any()) } returns savedTags
        every { taggedPostRepository.saveAll<TaggedPost>(any()) } returns TaggedPostFixture.savedTaggedPosts(post = savedPost, tags = savedTags)

        //when
        val postId = postService.create(adminAuthMember.id, cpd) // 질문 작성

        //then
        verify (exactly = 1){ postRepository.save(any()) }
        verify(exactly = 1) { tagRepository.saveAll<Tag>(any()) }
        verify(exactly = 1) { taggedPostRepository.saveAll<TaggedPost>(any()) }

    }




    @Test
    fun `공지 작성 실패 - 어드민이 아닌 경우(일반 유저)`() {

        //given
        val cpd = createPostDto(postType = NOTICE)

        //when
        val exceptionType =
            assertThrows<PostException> { postService.create(basicAuthMember.id, cpd) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
        verify (exactly = 0){ postRepository.save(any()) }
    }




    @Test
    fun `공지 작성 실패 - 어드민이 아닌 경우(블랙리스트)`() {

        //given
        val cpd = createPostDto(postType = NOTICE)

        //when
        val exceptionType =
            assertThrows<PostException> { postService.create(blackAuthMember.id, cpd) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
        verify (exactly = 0){ postRepository.save(any()) }
    }





    @Test
    fun `질문 수정 성공`() {

        //given
        val postId = 10L
        val savedTags = TagFixture.savedTags(3)
        val post = post(id = postId, postType = QUESTION, writerId = basicAuthMember.id, writerRole = basicAuthMember.role)
        every { postRepository.findByIdOrNull(postId) } returns post
        every { taggedPostRepository.findByPost(any()) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs
        every { tagRepository.saveAll<Tag>(any()) } returns savedTags
        every { taggedPostRepository.saveAll<TaggedPost>(any()) } returns TaggedPostFixture.savedTaggedPosts(post = post, tags = savedTags)

        //when
        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
        postService.update(basicAuthMember.id, postId, upd)


        //then
        verify(exactly = 1) { memberRepository.findByIdOrNull(basicAuthMember.id) }
    }



    @Test
    fun `질문 수정 실패 - 자신의 질문이 아닌 경우`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId,postType = QUESTION, writerId = basicAuthMember.id, writerRole = basicAuthMember.role )


        //when
        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
        val exceptionType =
            assertThrows<PostException> { postService.update(adminAuthMember.id, postId, upd) }.exceptionType()


        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST)

    }





    @Test
    fun `공지 수정 성공 - 자신(관리자)의 공지`() {

        //given
        val postId = 10L
        val post = post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = adminAuthMember.role)
        val savedTags = TagFixture.savedTags(3)
        every { postRepository.findByIdOrNull(postId) } returns post
        every { taggedPostRepository.findByPost(any()) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs
        every { tagRepository.saveAll<Tag>(any()) } returns savedTags
        every { taggedPostRepository.saveAll<TaggedPost>(any()) } returns TaggedPostFixture.savedTaggedPosts(post= post, tags = savedTags)

        //when
        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
        postService.update(adminAuthMember.id, postId, upd)


        //then
        verify(exactly = 1) { memberRepository.findByIdOrNull(adminAuthMember.id) }
    }





    @Test
    fun `공지 수정 실패 - 자신의 공지이나 관리자 자격을 박탈당한 경우 ( 블랙리스트 )`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = basicAuthMember.role )
        every { memberRepository.findByIdOrNull(adminAuthMember.id) } returns member(id = adminAuthMember.id, authority = BLACK) //회원 권한 변경


        //when
        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
        val exceptionType =
            assertThrows<PostException> { postService.update(adminAuthMember.id, postId, upd) }.exceptionType()


        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST)
        verify(exactly = 1) { memberRepository.findByIdOrNull(adminAuthMember.id) }
    }






    @Test
    fun `공지 수정 실패 - 자신의 공지이나 관리자 자격을 박탈당한 경우 ( 일반 회원 )`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = basicAuthMember.role )
        every { memberRepository.findByIdOrNull(adminAuthMember.id) } returns member(id = adminAuthMember.id, authority = BASIC) //회원 권한 변경


        //when
        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
        val exceptionType =
            assertThrows<PostException> { postService.update(adminAuthMember.id, postId, upd) }.exceptionType()


        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST)
        verify(exactly = 1) { memberRepository.findByIdOrNull(adminAuthMember.id) }
    }



    @Test
    fun `공지 수정 성공 - 다른 관리자가 등록한 게시물`() {

        //given
        val postId = 10L
        val post = post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = adminAuthMember.role)
        val savedTags = TagFixture.savedTags(3)
        val anotherAdminId = 20L
        every { postRepository.findByIdOrNull(postId) } returns post
        every { memberRepository.findByIdOrNull(anotherAdminId) } returns member(id = anotherAdminId, authority = ADMIN)
        every { tagRepository.saveAll<Tag>(any()) } returns savedTags
        every { taggedPostRepository.findByPost(any()) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs
        every { taggedPostRepository.saveAll<TaggedPost>(any()) } returns TaggedPostFixture.savedTaggedPosts(post = post, tags = savedTags)



        //when
        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
        postService.update(anotherAdminId, postId, upd)


        //then
        verify(exactly = 1) { memberRepository.findByIdOrNull(anotherAdminId) }
    }





    @Test
    fun `공지 수정 성공 - 여러 관리자가 동시에 수정할 경우, 가장 처음 수정한 사람 적용, 나머지는 예외 발생`() {

        //given
        TODO("공지 수정 성공 - 여러 관리자가 동시에 수정할 경우, 가장 처음 수정한 사람 적용, 나머지는 예외 발생")
        /*
            멀티스레드 테스트 할 때 영속성 컨텍스트가 공유되지 않음
            https://dulajra.medium.com/spring-transaction-management-over-multiple-threads-dzone-java-b36a5bc342e5

            그리고 테스트케이스에 @Transactional이 붙어있으면 먼가 잘 안됐음

            이건 그 예시인데, @Transactional 없이 테스트 한 것을 확인할 수 있음.
            https://4whomtbts.tistory.com/118
         */
    }






    @Test
    fun `질문 삭제 성공 - 자신의 게시물인 경우 - 작성된 댓글 & 대댓글 모두 제거`() {

        //given
        val postId = 10L
        val post =
            post(id = postId, postType = QUESTION, writerId = basicAuthMember.id, writerRole = basicAuthMember.role)
        every { postRepository.findByIdOrNull(postId) } returns post
        every { postRepository.delete(any()) } just runs
        every { taggedPostRepository.findByPost(post) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs


        //when
        postService.delete(basicAuthMember.id!!, postId)

        //then
        verify( exactly =  1 ) { postRepository.delete(any()) }


        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
    }



    @Test
    fun `질문 삭제 성공 - 관리자가 삭제하는 경우 - 작성된 댓글 & 대댓글 모두 제거`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = QUESTION, writerId = basicAuthMember.id, writerRole = basicAuthMember.role )
        every { postRepository.delete(any()) } just runs
        every { taggedPostRepository.findByPost(any()) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs


        //when
        postService.delete(adminAuthMember.id!!, postId)

        //then
        verify( exactly =  1 ) { postRepository.delete(any()) }

        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
    }


    @Test
    fun `질문 삭제 실패 - 자신의 게시물이 아닌 경우`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = QUESTION, writerId = basicAuthMember.id, writerRole = basicAuthMember.role )
        every { postRepository.delete(any()) } just runs

        val anotherBasicId = 200L
        every { memberRepository.findByIdOrNull(anotherBasicId) } returns member(id = anotherBasicId, authority = BASIC)




        //when
        val exceptionType =
            assertThrows<PostException> { postService.delete(anotherBasicId, postId) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST)
        verify (exactly = 0) { postRepository.delete(any()) }

    }





    @Test
    fun `공지 삭제 성공 - 자신의 공지`() {

        //given
        val postId = 10L
        val post = post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = adminAuthMember.role)


        every { postRepository.findByIdOrNull(postId) } returns post
        every { postRepository.delete(any()) } just runs
        every { taggedPostRepository.findByPost(any()) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs



        //when
        postService.delete(adminAuthMember.id!!, postId)

        //then
        verify( exactly =  1 ) { postRepository.delete(any()) }


        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
    }




    @Test
    fun `공지 삭제 실패 - 자신의 공지이나 관리자 자격을 박탈당해 일반 회원이 된 경우`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = basicAuthMember.role )
        every { postRepository.delete(any()) } just runs

        every { memberRepository.findByIdOrNull(adminAuthMember.id) } returns member(id = adminAuthMember.id, authority = BASIC)


        //when
        val exceptionType =
            assertThrows<PostException> { postService.delete(adminAuthMember.id!!, postId) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST)
        verify( exactly =  0 ) { postRepository.delete(any()) }
    }



    @Test
    fun `공지 삭제 실패 - 자신의 공지이나 관리자 자격을 박탈당해 블랙리스트가 된 경우`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = NOTICE, writerId = adminAuthMember.id, writerRole = blackAuthMember.role )
        every { postRepository.delete(any()) } just runs

        every { memberRepository.findByIdOrNull(adminAuthMember.id) } returns member(id = adminAuthMember.id, authority = BLACK)


        //when
        val exceptionType =
            assertThrows<PostException> { postService.delete(adminAuthMember.id!!, postId) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST)
        verify( exactly =  0 ) { postRepository.delete(any()) }
    }


    @Test
    fun `공지 삭제 성공 - 다른 관리자의 공지`() {

        //given
        val postId = 10L
        every { postRepository.findByIdOrNull(postId) } returns post(id = postId, postType = NOTICE, writerId = basicAuthMember.id, writerRole = basicAuthMember.role )
        every { postRepository.delete(any()) } just runs
        every { taggedPostRepository.findByPost(any()) } returns emptyList()
        every { taggedPostRepository.deleteAllInBatch(any()) } just runs

        val anotherAdminId = 300L
        every { memberRepository.findByIdOrNull(anotherAdminId) } returns member(id = anotherAdminId, authority = ADMIN)

        //when
        postService.delete(anotherAdminId, postId)

        //then
        verify( exactly =  1 ) { postRepository.delete(any()) }

        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
    }


    @Test
    fun `게시글 검색 - 결과 여러개인 경우`() {
        //given
        val totalElement = 100L
        val searchPage = 2
        val searchSize = 10
        val totalPage = (totalElement / searchSize).toInt()

        val postSearchCond = postSearchCond(title = null, content = null, postType = null)
        val postPageable = postPageable(page = searchPage, size = searchSize, orders = emptyList())

        val posts = mutableListOf<Post>()
        repeat(searchSize) {
            posts.add(post(id = it.toLong(), writerId = 1L, writerRole = BASIC))
        }

        val searchResult = PageImpl(posts, postPageable, totalElement)

        every { postRepository.search(postSearchCond, postPageable) } returns searchResult

        //when
        val result = postService.search(postSearchCond, postPageable)

        //then

        val expectedResult = SearchResultDto(
            totalPage = totalPage,
            totalElementCount = totalElement,
            currentPage = searchPage,
            currentElementCount = searchSize,
            simpleDtos = searchResult.content.map { SimplePostDto.from(it) })
        assertThat(result).isEqualTo(expectedResult)
    }


    @Test
    fun `게시글 검색 - 결과 1개인 경우`() {
        //given
        val totalElement = 100L
        val searchPage = 2
        val searchSize = 10
        val totalPage = (totalElement / searchSize).toInt()

        val postSearchCond = postSearchCond(title = null, content = null, postType = null)
        val postPageable = postPageable(page = searchPage, size = searchSize, orders = emptyList())

        val posts = mutableListOf<Post>()
        repeat(1) {
            posts.add(post(id = it.toLong(), writerId = 1L, writerRole = BASIC))
        }

        val searchResult = PageImpl(posts, postPageable, totalElement)

        every { postRepository.search(postSearchCond, postPageable) } returns searchResult

        //when
        val result = postService.search(postSearchCond, postPageable)

        //then

        val expectedResult = SearchResultDto(
            totalPage = totalPage,
            totalElementCount = totalElement,
            currentPage = searchPage,
            currentElementCount = 1,
            simpleDtos = searchResult.content.map { SimplePostDto.from(it) })
        assertThat(result).isEqualTo(expectedResult)
        assertThat(result.currentElementCount).isEqualTo(1)
    }

    @Test
    fun `게시글 검색 - 결과 0개인 경우`() {
        //given
        val totalElement = 100L
        val searchPage = 2
        val searchSize = 10
        val totalPage = (totalElement / searchSize).toInt()

        val postSearchCond = postSearchCond(title = null, content = null, postType = null)
        val postPageable = postPageable(page = searchPage, size = searchSize, orders = emptyList())

        val posts = mutableListOf<Post>()

        val searchResult = PageImpl(posts, postPageable, totalElement)

        every { postRepository.search(postSearchCond, postPageable) } returns searchResult

        //when
        val result = postService.search(postSearchCond, postPageable)

        //then

        val expectedResult = SearchResultDto(
            totalPage = totalPage,
            totalElementCount = totalElement,
            currentPage = searchPage,
            currentElementCount = 0,
            simpleDtos = searchResult.content.map { SimplePostDto.from(it) })
        assertThat(result).isEqualTo(expectedResult)
        assertThat(result.currentElementCount).isEqualTo(0)
        assertThat(result.simpleDtos.size).isEqualTo(0)
    }
}