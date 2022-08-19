//package com.whyrano.domain.post.service
//
//import com.whyrano.domain.member.entity.Role
//import com.whyrano.domain.member.entity.Role.BASIC
//import com.whyrano.domain.member.entity.Role.BLACK
//import com.whyrano.domain.member.fixture.MemberFixture
//import com.whyrano.domain.member.fixture.MemberFixture.member
//import com.whyrano.domain.member.repository.MemberRepository
//import com.whyrano.domain.post.entity.Type.NOTICE
//import com.whyrano.domain.post.entity.Type.QUESTION
//import com.whyrano.domain.post.exception.PostException
//import com.whyrano.domain.post.exception.PostExceptionType
//import com.whyrano.domain.post.fixture.PostFixture.UPDATE_CONTENT
//import com.whyrano.domain.post.fixture.PostFixture.UPDATE_TITLE
//import com.whyrano.domain.post.fixture.PostFixture.createPostDto
//import com.whyrano.domain.post.fixture.PostFixture.updatePostDto
//import com.whyrano.domain.post.repository.PostRepository
//import com.whyrano.global.auth.userdetails.AuthMember
//import com.whyrano.global.config.JpaConfig
//import com.whyrano.global.config.QuerydslConfig
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
//import org.springframework.context.annotation.Import
//import org.springframework.data.repository.findByIdOrNull
//import org.springframework.transaction.annotation.Transactional
//import javax.persistence.EntityManager
//
///**
// * Created by ShinD on 2022/08/14.
// */
//@DataJpaTest
//@Import(JpaConfig::class, QuerydslConfig::class)
//@Transactional
//internal class PostServiceTest3 {
//
//
//    /**
//     * 질문 작성
//     * 질문 수정
//     * 질문 삭제
//     *
//     */
//
//
//    private lateinit var postService: PostService
//
//    @Autowired private lateinit var postRepository: PostRepository
//    @Autowired private lateinit var memberRepository: MemberRepository
//    @Autowired private lateinit var em: EntityManager
//
//    private lateinit var basicAuthMember: AuthMember // 일반 회원
//    private lateinit var adminAuthMember: AuthMember // 관리자
//    private lateinit var blackAuthMember: AuthMember // 블랙리스트
//
//
//    @BeforeEach
//    fun setUp() {
//        //PostService 세팅
//        postService = PostService(memberRepository, postRepository)
//
//        // 회원들 DB에 저장
//        val basic = memberRepository.save(MemberFixture.member(id = null, authority = BASIC, email = "basic@example.com"))
//        val admin = memberRepository.save(MemberFixture.member(id = null, authority = Role.ADMIN, email = "admin@example.com"))
//        val black = memberRepository.save(MemberFixture.member(id = null, authority = Role.BLACK, email = "black@example.com"))
//
//        // 회원들 인증정보 세팅
//        basicAuthMember = MemberFixture.authMember(id = basic.id!!, email = basic.email, role = BASIC)
//        adminAuthMember =MemberFixture.authMember(id = admin.id!!,  email = admin.email, role = Role.ADMIN)
//        blackAuthMember = MemberFixture.authMember(id = black.id!!,  email = black.email, role = Role.BLACK)
//    }
//
//
//
//
//    /**
//     * 영속성 컨텍스트 비우기
//     */
//    private fun clearPersistenceContext() {
//        em.flush()
//        em.clear()
//    }
//
//
//
//
//
//    @Test
//    fun `질문 작성 성공`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION) // 질문 생성 DTO
//
//        //when
//        val postId = postService.create(basicAuthMember.id, cpd) // 질문 작성
//
//        //then
//        val savedPost = postRepository.findByIdOrNull(postId)
//        assertThat(savedPost).isNotNull // 작성된 질문은 null이 아니어야 함
//        assertThat(savedPost!!.title).isEqualTo(cpd.title)
//        assertThat(savedPost.content).isEqualTo(cpd.content)
//        assertThat(savedPost.answerCount).isEqualTo(0)
//        assertThat(savedPost.likeCount).isEqualTo(0)
//        assertThat(savedPost.viewCount).isEqualTo(0)
//        assertThat(savedPost.type).isEqualTo(QUESTION)
//    }
//
//
//
//
//    @Test
//    fun `질문 작성 실패 - 블랙리스트인 경우`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION) // 질문 생성 DTO
//
//        //when
//        val exceptionType =  // 블랙리스트가 질문을 작성하려는 경우
//            assertThrows<PostException> { postService.create(blackAuthMember.id, cpd) }.exceptionType()
//
//        //then
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_QUESTION)
//        assertThat(postRepository.findAll()).isEmpty()
//
//    }
//
//
//
//
//    @Test
//    fun `공지 작성 성공`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//
//        //when
//        val postId = postService.create(adminAuthMember.id, cpd)
//
//        //then
//        val findPost = postRepository.findByIdOrNull(postId)
//        assertThat(findPost!!.title).isEqualTo(cpd.title)
//        assertThat(findPost.type).isEqualTo(cpd.type)
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.answerCount).isEqualTo(0)
//        assertThat(findPost.viewCount).isEqualTo(0)
//        assertThat(findPost.likeCount).isEqualTo(0)
//        assertThat(findPost.writer!!.email).isEqualTo(adminAuthMember.email)
//        assertThat(findPost.writer!!.id).isEqualTo(adminAuthMember.id)
//    }
//
//
//
//
//    @Test
//    fun `공지 작성 실패 - 어드민이 아닌 경우(일반 유저)`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//
//        //when
//        val exceptionType =
//            assertThrows<PostException> { postService.create(basicAuthMember.id, cpd) }.exceptionType()
//
//        //then
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
//        assertThat(postRepository.findAll()).isEmpty()
//    }
//
//
//
//
//    @Test
//    fun `공지 작성 실패 - 어드민이 아닌 경우(블랙리스트)`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//
//        //when
//        val exceptionType =
//            assertThrows<PostException> { postService.create(blackAuthMember.id, cpd) }.exceptionType()
//
//        //then
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
//        assertThat(postRepository.findAll()).isEmpty()
//    }
//
//
//
//
//
//    @Test
//    fun `질문 수정 성공`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION)
//        val postId = postService.create(basicAuthMember.id, cpd)
//
//
//        //when
//        val upd = updatePostDto(title = UPDATE_TITLE, content = null)
//        postService.update(basicAuthMember.id, postId, upd)
//
//
//        //then
//        val findPost = postRepository.findByIdOrNull(postId)!!
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.title).isEqualTo(upd.title)
//        assertThat(findPost.title).isNotEqualTo(cpd.title)
//    }
//
//
//
//    @Test
//    fun `질문 수정 실패 - 자신의 질문이 아닌 경우`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION)
//        val postId = postService.create(basicAuthMember.id, cpd)
//
//
//        //when
//        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
//        val exceptionType =
//            assertThrows<PostException> { postService.update(adminAuthMember.id, postId, upd) }.exceptionType()
//
//
//        //then
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST)
//
//        val findPost = postRepository.findByIdOrNull(postId)!!
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.title).isEqualTo(cpd.title)
//        assertThat(findPost.title).isNotEqualTo(upd.title)
//        assertThat(findPost.content).isNotEqualTo(upd.content)
//    }
//
//
//
//
//
//    @Test
//    fun `공지 수정 성공 - 자신(관리자)의 공지`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//
//
//        //when
//        val upd = updatePostDto(title = UPDATE_TITLE, content = null)
//        postService.update(adminAuthMember.id, postId, upd)
//
//
//
//        //then
//        val findPost = postRepository.findByIdOrNull(postId)!!
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.title).isEqualTo(upd.title)
//        assertThat(findPost.title).isNotEqualTo(cpd.title)
//    }
//
//
//
//
//
//    @Test
//    fun `공지 수정 실패 - 자신의 공지이나 관리자 자격을 박탈당한 경우 ( 블랙리스트 )`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//
//        val member = memberRepository.findByIdOrNull(adminAuthMember.id)!!
//        member.changRole(Role.BLACK)    //회원 권한 변경
//        clearPersistenceContext()
//
//
//        //when
//        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
//        val exceptionType =
//            assertThrows<PostException> { postService.update(adminAuthMember.id, postId, upd) }.exceptionType()
//
//
//        //then
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST)
//
//        val findPost = postRepository.findByIdOrNull(postId)!!
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.title).isEqualTo(cpd.title)
//        assertThat(findPost.title).isNotEqualTo(upd.title)
//        assertThat(findPost.content).isNotEqualTo(upd.content)
//    }
//
//
//
//
//
//
//    @Test
//    fun `공지 수정 실패 - 자신의 공지이나 관리자 자격을 박탈당한 경우 ( 일반 회원 )`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//
//
//        val member = memberRepository.findByIdOrNull(adminAuthMember.id)!!
//        member.changRole(BASIC)    //회원 권한 변경
//
//        clearPersistenceContext()
//
//
//        //when
//        val upd = updatePostDto(title = UPDATE_TITLE, content = UPDATE_CONTENT)
//        val exceptionType =
//            assertThrows<PostException> { postService.update(adminAuthMember.id, postId, upd) }.exceptionType()
//
//
//        //then
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_UPDATE_POST)
//        val findPost = postRepository.findByIdOrNull(postId)!!
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.title).isEqualTo(cpd.title)
//        assertThat(findPost.title).isNotEqualTo(upd.title)
//        assertThat(findPost.content).isNotEqualTo(upd.content)
//    }
//
//
//
//    @Test
//    fun `공지 수정 성공 - 다른 관리자가 등록한 게시물`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//
//        val anotherAdmin = memberRepository.save(member(authority = Role.ADMIN, email = "admin2@example.com"))
//        clearPersistenceContext()
//
//
//        //when
//        val upd = updatePostDto(title = UPDATE_TITLE, content = null)
//        postService.update(anotherAdmin.id!!, postId, upd)
//
//
//
//        //then
//        val findPost = postRepository.findByIdOrNull(postId)!!
//        assertThat(findPost.content).isEqualTo(cpd.content)
//        assertThat(findPost.title).isEqualTo(upd.title)
//        assertThat(findPost.title).isNotEqualTo(cpd.title)
//    }
//
//
//
//
//    @Test
//    fun `공지 수정 성공 - 여러 관리자가 동시에 수정할 경우, 가장 처음 수정한 사람 적용, 나머지는 예외 발생`() {
//
//        //given
//        TODO("공지 수정 성공 - 여러 관리자가 동시에 수정할 경우, 가장 처음 수정한 사람 적용, 나머지는 예외 발생")
//        /*
//            멀티스레드 테스트 할 때 영속성 컨텍스트가 공유되지 않음
//            https://dulajra.medium.com/spring-transaction-management-over-multiple-threads-dzone-java-b36a5bc342e5
//
//            그리고 테스트케이스에 @Transactional이 붙어있으면 먼가 잘 안됐음
//
//            이건 그 예시인데, @Transactional 없이 테스트 한 것을 확인할 수 있음.
//            https://4whomtbts.tistory.com/118
//         */
//    }
//
//
//
//
//
//
//    @Test
//    fun `질문 삭제 성공 - 자신의 게시물인 경우 - 작성된 댓글 & 대댓글 모두 제거`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION)
//        val postId = postService.create(basicAuthMember.id, cpd)
//        clearPersistenceContext()
//
//
//        //when
//        postService.delete(basicAuthMember.id!!, postId)
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNull()
//
//        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
//    }
//
//
//
//    @Test
//    fun `질문 삭제 성공 - 관리자가 삭제하는 경우 - 작성된 댓글 & 대댓글 모두 제거`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION)
//        val postId = postService.create(basicAuthMember.id, cpd)
//        clearPersistenceContext()
//
//
//        //when
//        postService.delete(adminAuthMember.id!!, postId)
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNull()
//
//        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
//    }
//
//    @Test
//    fun `질문 삭제 실패 - 자신의 게시물이 아닌 경우`() {
//
//        //given
//        val cpd = createPostDto(type = QUESTION)
//        val postId = postService.create(basicAuthMember.id, cpd)
//
//        val anotherBasic = memberRepository.save(member(authority = BASIC, email = "basic2@example.com"))
//        clearPersistenceContext()
//
//
//        //when
//        val exceptionType =
//            assertThrows<PostException> { postService.delete(anotherBasic.id!!, postId) }.exceptionType()
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNotNull
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST)
//    }
//
//
//
//
//
//    @Test
//    fun `공지 삭제 성공 - 자신의 공지`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//        clearPersistenceContext()
//
//
//        //when
//        postService.delete(adminAuthMember.id!!, postId)
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNull()
//
//        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
//    }
//
//
//
//
//    @Test
//    fun `공지 삭제 실패 - 자신의 공지이나 관리자 자격을 박탈당해 일반 회원이 된 경우`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//        val writer = memberRepository.findByIdOrNull(adminAuthMember.id)!!
//        writer.changRole(BASIC)
//
//        clearPersistenceContext()
//
//
//        //when
//        val exceptionType =
//            assertThrows<PostException> { postService.delete(writer.id!!, postId) }.exceptionType()
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNotNull
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST)
//    }
//
//    @Test
//    fun `공지 삭제 실패 - 자신의 공지이나 관리자 자격을 박탈당해 블랙리스트가 된 경우`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//        val writer = memberRepository.findByIdOrNull(adminAuthMember.id)!!
//        writer.changRole(BLACK)
//
//        clearPersistenceContext()
//
//
//        //when
//        val exceptionType =
//            assertThrows<PostException> { postService.delete(writer.id!!, postId) }.exceptionType()
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNotNull
//        assertThat(exceptionType).isEqualTo(PostExceptionType.NO_AUTHORITY_DELETE_POST)
//    }
//
//    @Test
//    fun `공지 삭제 성공 - 다른 관리자의 공지`() {
//
//        //given
//        val cpd = createPostDto(type = NOTICE)
//        val postId = postService.create(adminAuthMember.id, cpd)
//
//        val anotherAdmin = memberRepository.save(member(authority = Role.ADMIN, email = "admin2@example.com"))
//        clearPersistenceContext()
//
//
//        //when
//        postService.delete(anotherAdmin.id!!, postId)
//
//        //then
//        assertThat(postRepository.findByIdOrNull(postId)).isNull()
//
//        //TODO 댓글, 대댓글 모두 제거하는지 구현해야 함
//    }
//}