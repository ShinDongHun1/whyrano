package com.whyrano.domain.post.service

import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.entity.Type
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.fixture.PostFixture
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.global.auth.userdetails.AuthMember
import com.whyrano.global.config.JpaConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

/**
 * Created by ShinD on 2022/08/14.
 */
@Import(JpaConfig::class)
@DataJpaTest
@Transactional
internal class PostServiceTest {

    private lateinit var postService: PostService

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private var basicAuthMember = MemberFixture.authMember(id = 1L, role = Role.BASIC)
    private var adminAuthMember = MemberFixture.authMember(id = 1L, role = Role.ADMIN)
    private var blackAuthMember = MemberFixture.authMember(id = 1L, role = Role.BLACK)

    /**
     * 질문 작성
     * 질문 수정
     * 질문 삭제
     * 질문 검색
     */

    @BeforeEach
    fun setUp() {
        postService = PostService(memberRepository, postRepository)


        val basic = memberRepository.save(MemberFixture.member(id = null, authority = Role.BASIC, email = "basic@example.com"))
        val admin = memberRepository.save(MemberFixture.member(id = null, authority = Role.ADMIN, email = "admin@example.com"))
        val black = memberRepository.save(MemberFixture.member(id = null, authority = Role.BLACK, email = "black@example.com"))


        basicAuthMember = MemberFixture.authMember(id = basic.id!!, email = basic.email, role = Role.BASIC)
        adminAuthMember =MemberFixture.authMember(id = admin.id!!,  email = admin.email, role = Role.ADMIN)
        blackAuthMember = MemberFixture.authMember(id = black.id!!,  email = black.email, role = Role.BLACK)
    }



    private fun saveAuthMemberInSecurityContext(authMember: AuthMember) {
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(authMember, null, authMember.authorities)
        SecurityContextHolder.setContext(context)
    }



    @Test
    fun `질문 작성 성공`() {
        //given
        val cpd = PostFixture.createPostDto(type = Type.QUESTION)
        saveAuthMemberInSecurityContext(basicAuthMember)

        //when
        val postId = postService.createPost(basicAuthMember.id, cpd)

        //then
        assertThat(postId).isNotNull

    }


    @Test
    fun `질문 작성 실패 - 블랙리스트인 경우`() {
        //given
        val cpd = PostFixture.createPostDto(type = Type.QUESTION)
        saveAuthMemberInSecurityContext(blackAuthMember)

        //when
        val exceptionType = assertThrows<PostException> { postService.createPost(blackAuthMember.id, cpd) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_POST)
        assertThat(postRepository.findAll()).isEmpty()

    }

    @Test
    fun `공지 작성 성공`() {
        //given
        val cpd = PostFixture.createPostDto(type = Type.NOTICE)
        saveAuthMemberInSecurityContext(adminAuthMember)

        //when
        val postId = postService.createPost(adminAuthMember.id, cpd)

        //then
        assertThat(postId).isNotNull
        val findPost = postRepository.findByIdOrNull(postId)
        assertThat(findPost!!).isNotNull

        assertThat(findPost.title).isEqualTo(cpd.title)
        assertThat(findPost.type).isEqualTo(cpd.type)
        assertThat(findPost.content).isEqualTo(cpd.content)
        assertThat(findPost.answerCount).isEqualTo(0)
        assertThat(findPost.viewCount).isEqualTo(0)
        assertThat(findPost.likeCount).isEqualTo(0)
        assertThat(findPost.writer!!.email).isEqualTo(adminAuthMember.email)
        assertThat(findPost.writer!!.id).isEqualTo(adminAuthMember.id)
    }

    @Test
    fun `공지 작성 실패 - 어드민이 아닌 경우(일반 유저)`() {
        //given
        val cpd = PostFixture.createPostDto(type = Type.NOTICE)
        saveAuthMemberInSecurityContext(basicAuthMember)

        //when
        val exceptionType = assertThrows<PostException> { postService.createPost(basicAuthMember.id, cpd) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
        assertThat(postRepository.findAll()).isEmpty()
    }

    @Test
    fun `공지 작성 실패 - 어드민이 아닌 경우(블랙리스트)`() {
        //given
        val cpd = PostFixture.createPostDto(type = Type.NOTICE)
        saveAuthMemberInSecurityContext(blackAuthMember)

        //when
        val exceptionType = assertThrows<PostException> { postService.createPost(blackAuthMember.id, cpd) }.exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
        assertThat(postRepository.findAll()).isEmpty()
    }


    @Test
    fun `질문 수정 성공`() {

    }

    @Test
    fun `질문 수정 실패 - 자신의 질문이 아닌 경우`() {

    }

    @Test
    fun `공지 수정 성공 - 자신(관리자)의 공지`() {

    }

    @Test
    fun `공지 수정 실패 - 자신의 공지이나 관리자 자격을 박탈당한 경우`() {

    }

    @Test
    fun `공지 수정 성공 - 다른 관리자가 등록한 게시물`() {

    }

    @Test
    fun `공지 수정 실패 - 관리자가 아닌 경우`() {

    }

    @Test
    fun `질문 삭제 성공 - 작성된 댓글 & 대댓글 모두 제거`() {

    }
    @Test
    fun `질문 삭제 실패 - 자신의 게시물이 아닌 경우`() {

    }

    @Test
    fun `공지 삭제 성공 - 자신의 공지`() {
    }

    @Test
    fun `공지 삭제 실패 - 자신의 공지이나 관리자 자격을 박탈당한 경우`() {

    }


    @Test
    fun `공지 삭제 성공 - 다른 관리자의 공지`() {
    }


    @Test
    fun `공지 삭제 실패 - 관리자가 아닌 경우`() {
    }
}