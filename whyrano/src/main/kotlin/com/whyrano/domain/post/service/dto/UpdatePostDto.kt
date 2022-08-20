package com.whyrano.domain.post.service.dto

import com.whyrano.domain.tag.dto.TagDto

/**
 * Created by ShinD on 2022/08/15.
 */
data class UpdatePostDto(

    val content: String,

    val title: String,

    val tags: List<TagDto> = emptyList(),
) {

    fun getTagEntities() =
        tags.map(TagDto::toEntity)
}
