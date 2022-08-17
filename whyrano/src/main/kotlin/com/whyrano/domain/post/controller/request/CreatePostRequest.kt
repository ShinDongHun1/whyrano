package com.whyrano.domain.post.controller.request

import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.service.dto.CreatePostDto
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

/**
 * Created by ShinD on 2022/08/16.
 */
data class CreatePostRequest(
    @field:NotNull val postType: PostType,  // Post 타입

    @field:NotBlank val content: String,     // 게시글 내용

    @field:NotBlank val title: String,       // 게시글 제목
) {

    fun toServiceDto() =
        CreatePostDto(
            postType = postType,
            content = content,
            title = title,
        )
}