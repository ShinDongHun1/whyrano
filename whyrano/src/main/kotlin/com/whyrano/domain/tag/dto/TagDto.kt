package com.whyrano.domain.tag.dto

import com.whyrano.domain.tag.entity.Tag

/**
 * Created by ShinD on 2022/08/19.
 */
data class TagDto(

    val id: Long? = null, // null이 아닌 경우 이미 존재하는 태그

    val name: String,

) {
    fun toEntity(): Tag =
        Tag(id = id, name = name)
}