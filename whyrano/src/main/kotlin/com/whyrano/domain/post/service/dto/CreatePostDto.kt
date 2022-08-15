package com.whyrano.domain.post.service.dto

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType

data class CreatePostDto(
    val postType: PostType,
    val content: String,
    val title: String
) {
    fun toEntity() =
        Post(postType = postType , content = content, title = title)

}
