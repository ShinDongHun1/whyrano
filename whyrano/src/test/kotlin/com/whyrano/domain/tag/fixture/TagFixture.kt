package com.whyrano.domain.tag.fixture

import com.whyrano.domain.tag.dto.TagDto
import com.whyrano.domain.tag.entity.Tag
import java.util.stream.IntStream

/**
 * Created by ShinD on 2022/08/19.
 */
object TagFixture {

    private const val NAME = "TAG"
    private const val ID = 11L
    fun tagDto(
        id: Long? = ID,
        name: String = NAME
    ) =
        TagDto(id= id, name = name)

    fun newTags(
        size: Int = 3
    ): MutableList<Tag> {
        val result = mutableListOf<Tag>()
        for (i in 0 until size) {
            result.add(Tag(id = null, name = "TAG_{${i}}"))
        }
        return result
    }

    fun savedTags(
        size: Int = 3
    ): MutableList<Tag> {
        val result = mutableListOf<Tag>()
        for (i in 0 until size) {
            result.add(Tag(id = i.toLong(), name = "TAG_{${i}}"))
        }
        return result
    }

    fun newTagDtos(size: Int): MutableList<TagDto> {
        val result = mutableListOf<TagDto>()
        for (i in 0 until size) {
            result.add(TagDto(id = null, name = "TAG_{${i}}"))
        }
        return result
    }


    fun savedTagDtos(size: Int): MutableList<TagDto> {
        val result = mutableListOf<TagDto>()
        for (i in 0 until size) {
            result.add(TagDto(id = i.toLong(), name = "TAG_{${i}}"))
        }
        return result
    }
}