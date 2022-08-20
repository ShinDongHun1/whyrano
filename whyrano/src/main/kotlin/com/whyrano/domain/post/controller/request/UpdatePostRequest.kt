package com.whyrano.domain.post.controller.request

import com.whyrano.domain.post.service.dto.UpdatePostDto
import com.whyrano.domain.tag.dto.TagDto
import javax.validation.constraints.NotBlank

/**
 * Created by ShinD on 2022/08/16.
 */
data class UpdatePostRequest(

    @field:NotBlank val content: String,     // 게시글 내용

    @field:NotBlank val title: String,       // 게시글 제목

    val tags: List<TagDto> = emptyList(),

    ) {

    fun toServiceDto(): UpdatePostDto =
        UpdatePostDto(content = content, title = title, tags = tags)

}