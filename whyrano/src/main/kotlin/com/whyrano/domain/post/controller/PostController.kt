package com.whyrano.domain.post.controller

import com.whyrano.domain.post.controller.dto.CreatePostRequest
import com.whyrano.domain.post.service.PostService
import com.whyrano.global.auth.userdetails.AuthMember
import com.whyrano.global.web.argumentresolver.Auth
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath
import javax.validation.Valid

/**
 * Created by ShinD on 2022/08/16.
 */

@RestController
class PostController(

    private val postService: PostService,

) {





    /**
     * 게시물 생성
     * (공지랑 질문을 따로 작성하게 하려다, 일단 같이 해둠)
     */
    @PostMapping("/post")
    fun create(
        @Auth authMember: AuthMember, // 작성 요청을 보낸 회원
        @Valid @RequestBody createPostRequest: CreatePostRequest,
    ) : ResponseEntity<Unit> {

        val postId = postService.create(authMember.id, createPostRequest.toServiceDto())

        // url 생성
        val url = fromCurrentContextPath()
            .path("/post/{postId}")
            .buildAndExpand(postId)
            .toUri()

        return ResponseEntity.created(url).build()
    }
}