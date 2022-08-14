package com.whyrano.domain.post.service

import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.entity.Role.*
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType.NOT_FOUND
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.entity.Type
import com.whyrano.domain.post.entity.Type.NOTICE
import com.whyrano.domain.post.entity.Type.QUESTION
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.post.service.dto.CreatePostDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by ShinD on 2022/08/14.
 */
@Service
@Transactional
class PostService(
    private val memberRepository: MemberRepository,
    private val postRepository: PostRepository,
) {

    /**
     * 질문 작성
     * 질문 수정
     * 질문 삭제
     * 질문 검색
     *
     * TODO 태그 작성, 수정, 삭제
     * TODO 질문 조회 - 댓글, 답글 기능 구현 후 작성
     */

    /**
     * 질문, 공지 작성
     */
    fun createPost(
        writerId: Long,
        cpd: CreatePostDto
    ): Long {

        // 작성자 정보 조회
        val writer = memberRepository.findByIdOrNull(writerId) ?: throw MemberException(NOT_FOUND)

        val post = cpd.toEntity()

        // 작성자 권한 확인
        checkAuthority(writer.role, post.type)

        // 작성자 설정
        post.confirmWriter(writer)

        //저장 후 id 반환
        return postRepository.save(post).id!!
    }




    /**
     * 공지 - 어드민만 가능
     * 질문 - 어드민, 일반 유저 가능 (블랙리스트 불가능)
     */
    private fun checkAuthority(role: Role, type: Type){
        when(type) {
            NOTICE -> if (role != ADMIN) throw PostException(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)
            QUESTION -> if (role == BLACK) throw PostException(PostExceptionType.NO_AUTHORITY_CREATE_POST)
        }
    }
}