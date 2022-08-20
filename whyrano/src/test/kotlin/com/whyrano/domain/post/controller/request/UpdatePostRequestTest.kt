package com.whyrano.domain.post.controller.request

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.whyrano.domain.post.fixture.PostFixture.updatePostDto
import com.whyrano.domain.post.fixture.PostFixture.updatePostRequest
import com.whyrano.domain.tag.fixture.TagFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Created by ShinD on 2022/08/16.
 */
internal class UpdatePostRequestTest {


    companion object {

        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

        private const val JSON_FORMAT = """
            {
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
    fun `Json에서 UpdatePostRequest 로 변환 테스트`() {


        //given
        val content = "example content"
        val title = "example title"
        val newTags = TagFixture.newTagDtos(size = 2)
        val savedTags = TagFixture.savedTagDtos(size = 2)
        savedTags.addAll(newTags)


        val upr = updatePostRequest(content = content, title = title, tagDtos = savedTags)

        val format = JSON_FORMAT.format(
            content, title,
            savedTags[0].id, savedTags[0].name, savedTags[1].id, savedTags[1].name,
            newTags[0].id, newTags[0].name, newTags[1].id, newTags[1].name,
        )


        //when
        val readValue = objectMapper.readValue(format, UpdatePostRequest::class.java)

        //then
        assertThat(readValue).isEqualTo(upr)
    }



    @Test
    fun `UpdatePostRequest에서 UpdatePostDto 로 변환 테스트`() {

        //given
        val content = "example content"
        val title = "example title"

        val createPostRequest = updatePostRequest(content = content, title = title)
        val upd = updatePostDto(content = content, title = title)

        //when
        val toServiceDto = createPostRequest.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(upd)
    }

}