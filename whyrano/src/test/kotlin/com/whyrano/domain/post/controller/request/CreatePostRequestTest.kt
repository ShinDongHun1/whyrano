package com.whyrano.domain.post.controller.request

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.fixture.PostFixture.createPostDto
import com.whyrano.domain.post.fixture.PostFixture.createPostRequest
import com.whyrano.domain.tag.fixture.TagFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Created by ShinD on 2022/08/16.
 */
internal class CreatePostRequestTest {

    companion object {

        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        private const val JSON_FORMAT = """
            {
                "postType":"%s",
                "content":"%s",
                "title":"%s",
                "tags":[
                    {"id":"%s", "name":"%s"},
                    {"id":"%s", "name":"%s"},
                    {"id":"%s", "name":"%s"},
                    {"id":"%s", "name":"%s"}
                ]
            }
        """
    }



    @Test
    fun `json에서 CreatePostRequest로 변환 테스트`() {

        //given
        val postType = "QUESTION"
        val content = "example content"
        val title = "example title"
        val newTags = TagFixture.newTagDtos(size = 2)
        val savedTags = TagFixture.savedTagDtos(size = 2)
        savedTags.addAll(newTags)

        val createPostRequest =
            createPostRequest(postType = PostType.QUESTION, content = content, title = title, tagDtos = savedTags)

        val format = JSON_FORMAT.format(
            postType, content, title,
            savedTags[0].id, savedTags[0].name, savedTags[1].id, savedTags[1].name,
            newTags[0].id, newTags[0].name, newTags[1].id, newTags[1].name,
        )


        //when
        val readValue = objectMapper.readValue(format, CreatePostRequest::class.java)

        //then
        assertThat(readValue).isEqualTo(createPostRequest)
    }



    @Test
    fun `json에서 CreatePostRequest로 변환 테스트 - 타입이 ""인 경우 예외`() {

        //given
        val postType = ""
        val content = "example content"
        val title = "example title"
        val format = JSON_FORMAT.format(
            postType, content, title,
            "", "", "", "",
            "", "", "", ""
        )


        //when, then
        assertThrows<InvalidFormatException> { objectMapper.readValue(format, CreatePostRequest::class.java) }
    }



    @Test
    fun `json에서 CreatePostRequest로 변환 테스트 - 타입이 소문자인 경우 예외 `() {

        //given
        val postType = "question"
        val content = "example content"
        val title = "example title"
        val format = JSON_FORMAT.format(
            postType, content, title,
            "", "", "", "",
            "", "", "", ""
        )


        val createPostRequest = createPostRequest(postType = PostType.QUESTION, content = content, title = title)

        //when, then
        assertThrows<InvalidFormatException> { objectMapper.readValue(format, CreatePostRequest::class.java) }
    }



    @Test
    fun `CreatePostRequest에서 CreatePostDto로 변환 테스트`() {

        //given
        val content = "example content"
        val title = "example title"

        val createPostRequest = createPostRequest(postType = PostType.QUESTION, content = content, title = title)
        val createPostDto = createPostDto(postType = PostType.QUESTION, content = content, title = title)

        //when
        val toServiceDto = createPostRequest.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(createPostDto)
    }
}