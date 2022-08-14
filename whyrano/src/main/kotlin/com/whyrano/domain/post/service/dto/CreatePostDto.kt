package com.whyrano.domain.post.service.dto

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.Type

data class CreatePostDto(
    val type: Type,
    val content: String,
    val title: String
) {
    fun toEntity() =
        Post(type = type , content = content, title = title)

}
