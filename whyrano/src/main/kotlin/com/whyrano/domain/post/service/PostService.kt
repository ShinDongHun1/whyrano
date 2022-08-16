package com.whyrano.domain.post.service

import com.whyrano.domain.common.search.SearchResultDto
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType.NOT_FOUND
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.post.search.PostSearchCond
import com.whyrano.domain.post.service.dto.CreatePostDto
import com.whyrano.domain.post.service.dto.SimplePostDto
import com.whyrano.domain.post.service.dto.UpdatePostDto
import org.springframework.data.domain.Pageable
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
     * TODO 동시에 여러 관리자가 공지를 수정할 경우(동시성 문제 발생), 락을 걸어 처리하기
     */





    /**
     * 질문, 공지 작성
     */
    fun create(
        writerId: Long,
        cpd: CreatePostDto
    ): Long {

        // 작성자 정보 조회
        val writer = memberRepository.findByIdOrNull(writerId) ?: throw MemberException(NOT_FOUND)

        val post = cpd.toEntity()

        // 작성자 권한 확인 -> 없다면 예외 발생
        post.checkCreateAuthority(writer)

        // 작성자 설정
        post.confirmWriter(writer)

        //저장 후 id 반환
        return postRepository.save(post).id!!
    }





    /**
     * 질문, 공지 수정
     *
     * TODO 여러 관리자가 동시에 수정할 경우, 처리해야 함. @Version, @Lock 등 사용
     */
    fun update(
        writerId: Long,
        postId: Long,
        upd: UpdatePostDto
    ) {

        // Post 정보 조회
        val post = postRepository.findByIdOrNull(postId) ?: throw PostException(PostExceptionType.NOT_FOUND)

        // 작성자 조회
        val writer = memberRepository.findByIdOrNull(writerId) ?: throw MemberException(NOT_FOUND)

        // post 수정 권한 여부 확인 -> 없다면 예외 발생
        post.checkUpdateAuthority(writer)

        // post 수정
        post.update(upd.title, upd.content)
    }





    /**
     * 질문, 공지 삭제
     */
    fun delete(writerId: Long, postId: Long) {

        // Post 정보 조회
        val post = postRepository.findByIdOrNull(postId) ?: throw PostException(PostExceptionType.NOT_FOUND)

        // 작성자 조회
        val writer = memberRepository.findByIdOrNull(writerId) ?: throw MemberException(NOT_FOUND)

        // post 삭제 권한 여부 확인 -> 없다면 예외 발생
        post.checkDeleteAuthority(writer)

        // post 삭제
        postRepository.delete(post)
    }





    /**
     * 질문, 공지 검색
     *
     * 컨트롤러에서 page를 1씩 빼서 넘겨줌
     *
     * 즉 page=1 인 경우 page = 0으로 넘어오므로 정삭적으로 작동함
     */
    fun search(postSearchCond: PostSearchCond, pageable: Pageable): SearchResultDto<SimplePostDto> {

        // 검색 수행
        val result = postRepository.search(postSearchCond, pageable)

        // 검색 결과를 DTO로 변환
        val simplePostDtos = result.content.map { SimplePostDto.from(it) }

        return SearchResultDto(
            totalPage = result.totalPages,                  // 전체 페이지 수
            totalElementCount = result.totalElements,       // 전체 요소 수
            currentPage = result.number,                    // 현재 페이지가 몇 페이지인지 (0부터 시작하므로 1 더해서 넘겨주기)
            currentElementCount = result.numberOfElements,  // 현재 요소의 수가 몇개인지
            simpleDtos = simplePostDtos                     // 요소에 대한 간단한 정보를 담은 DTO
        )
    }





    /**
     * 게시글 단일 조회
     *
     * TODO 구현해야 함
     */
    fun findOne(postId: Long) {

        TODO("not implementation")
    }
}