package com.whyrano.domain.answer.service

import com.whyrano.domain.answer.repository.AnswerRepository
import com.whyrano.domain.answer.service.dto.CreateAnswerDto
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.repository.PostRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by ShinD on 2022/08/20.
 */
@Service
@Transactional
class AnswerService(

    private val memberRepository: MemberRepository,

    private val postRepository: PostRepository,

    private val answerRepository: AnswerRepository,

) {

    /**
     * 답변 작성
     * (블랙리스트는 불가능)
     *
     * 답변 수정
     * (블랙리스트는 불가능, 본인만 가능)
     *
     * 답변 삭제
     * (블랙리스트는 불가능, 어드민은 강제로 가능)
     *
     * 답변 조회 (포스트 조회 시 함께)
     *
     * TODO : 답변 좋아요
     */

    /**
     * 답변 작성
     * (블랙리스트는 답변 불가능)
     */
    fun create(
        writerId: Long,
        postId: Long,
        cad: CreateAnswerDto
    ): Long {

        val answer = cad.toEntity()

        // 작성자 조회
        val writer = memberRepository.findByIdOrNull(writerId) ?: throw MemberException(MemberExceptionType.NOT_FOUND)

        // 작성자 권한 확인(블랙리스트라면 예외 발생)과 함께 세팅하기
        answer.checkAuthorityAndSetWriter(writer)

        // 질문 조회
        val post = postRepository.findByIdOrNull(postId) ?: throw PostException(PostExceptionType.NOT_FOUND)

        // 질문 타입 확인(공지라면 예외 발생)과 함께 세팅하기
        answer.checkPostTypeAndSetPost(post)

        // writer와 post가 설정되어 있는지 마지막 체크 (그럴 일 없지만, 수정 시 빼먹을수도 있으므로)
        checkNotNull(answer.writer) { "writer is null!" }
        checkNotNull(answer.post) { "post is null!" }

        // 답변 저장
        return answerRepository.save(answer).id!!
    }


}