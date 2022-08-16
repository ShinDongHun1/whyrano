package com.whyrano.domain.post.service.dto

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType

data class CreatePostDto(

    val postType: PostType,  // Post 타입

    val content: String,     // 게시글 내용

    val title: String,       // 게시글 제목

) {
    fun toEntity() =
        Post(postType = postType , content = content, title = title)
}
