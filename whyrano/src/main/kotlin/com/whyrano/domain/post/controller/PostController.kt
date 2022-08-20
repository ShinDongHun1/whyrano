package com.whyrano.domain.post.controller

import com.whyrano.domain.common.search.SearchResultResponse
import com.whyrano.domain.post.controller.request.CreatePostRequest
import com.whyrano.domain.post.controller.request.UpdatePostRequest
import com.whyrano.domain.post.controller.response.SimplePostResponse
import com.whyrano.domain.post.search.PostSearchCond
import com.whyrano.domain.post.service.PostService
import com.whyrano.global.auth.userdetails.AuthMember
import com.whyrano.global.web.argumentresolver.auth.Auth
import com.whyrano.global.web.argumentresolver.pageable.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath
import javax.validation.Valid

/**
 * Created by ShinD on 2022/08/16.
 */

@RestController
@RequestMapping("/post")
class PostController(

    private val postService: PostService,

    ) {


    /**
     * 게시물 생성
     * (공지랑 질문을 따로 작성하게 하려다, 일단 같이 해둠)
     */
    @PostMapping
    fun create(
        @Auth authMember: AuthMember, // 작성 요청을 보낸 회원
        @Valid @RequestBody cpr: CreatePostRequest,
    ): ResponseEntity<Unit> {

        val postId = postService.create(authMember.id, cpr.toServiceDto())

        // url 생성
        val url = fromCurrentContextPath()
            .path("/post/{postId}")
            .buildAndExpand(postId)
            .toUri()

        return ResponseEntity.created(url).build()
    }



    /**
     * 게시물 수정
     */
    @PutMapping("/{postId}")
    fun update(
        @Auth authMember: AuthMember, // 작성 요청을 보낸 회원
        @PathVariable("postId") postId: Long,
        @RequestBody upr: UpdatePostRequest,
    ): ResponseEntity<Unit> {

        //upd = UpdatePostDto
        postService.update(writerId = authMember.id, postId = postId, upd = upr.toServiceDto())

        return ResponseEntity.ok().build()
    }



    /**
     * 게시물 삭제
     */
    @DeleteMapping("/{postId}")
    fun delete(
        @Auth authMember: AuthMember, // 작성 요청을 보낸 회원
        @PathVariable("postId") postId: Long,
    ): ResponseEntity<Unit> {

        postService.delete(writerId = authMember.id, postId = postId)

        return ResponseEntity.noContent().build()
    }



    /**
     * 게시물 단일 조회
     */
    @GetMapping("/{postId}")
    fun findOne(
        @Auth authMember: AuthMember, // 작성 요청을 보낸 회원
        @PathVariable("postId") postId: Long,
    ): ResponseEntity<Unit> {

        TODO("not implement")

        return ResponseEntity.ok().build()
    }



    /**
     * 게시물 검색
     *
     * Pageable 관련하여
     * https://okky.kr/article/971437
     * http://honeymon.io/tech/2018/03/13/spring-boot-mvc-controller.html
     * https://github.com/nwerl/nwerl-lolstats-webservice/issues/14
     *
     * example /post?content=example&page=2&sort=createdDate,desc&sort=createdDate,asc
     */
    @GetMapping
    fun search(
        @ModelAttribute cond: PostSearchCond,
        @Page pageable: Pageable,
    ): ResponseEntity<SearchResultResponse<SimplePostResponse>> {

        // pageable 에는 page가 1이 감소해서 받아짐 (시작 페이지가 1), srd = searchResultDto
        val srd = postService.search(postSearchCond = cond, pageable = pageable)

        // 응답 객체 생성
        val searchResultResponse = SearchResultResponse(
            totalPage = srd.totalPage,
            totalElementCount = srd.totalElementCount,
            currentPage = srd.currentPage + 1, // 1을 감소시켜서 전달하므로 반환할때는 1을 증가시켜주어야 함
            currentElementCount = srd.currentElementCount,
            simpleDataResponses = srd.simpleDtos.map { SimplePostResponse.from(it) }
        )

        return ResponseEntity.ok().body(searchResultResponse)
    }
}