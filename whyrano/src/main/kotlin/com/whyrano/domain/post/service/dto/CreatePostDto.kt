package com.whyrano.domain.post.service.dto

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.tag.dto.TagDto

data class CreatePostDto(

    val postType: PostType,  // Post 타입

    val content: String,     // 게시글 내용

    val title: String,       // 게시글 제목

    val tags: List<TagDto> = emptyList(),

    ) {

    fun toEntity(): Post =
        Post(postType = postType, content = content, title = title)

    fun getTagEntities() =
        tags.map(TagDto::toEntity)
}
