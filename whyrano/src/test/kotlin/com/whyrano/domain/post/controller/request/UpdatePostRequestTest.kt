package com.whyrano.domain.post.controller.request

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.whyrano.domain.post.fixture.PostFixture.updatePostDto
import com.whyrano.domain.post.fixture.PostFixture.updatePostRequest
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
                "title":"%s"
            }
        """
    }





    @Test
    fun `Json에서 UpdatePostRequest 로 변환 테스트`() {


        //given
        val content = "example content"
        val title = "example title"

        val upr = updatePostRequest(content = content, title = title)

        val format = JSON_FORMAT.format(content, title)


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





    @Test
    fun `UpdatePostRequest에서 UpdatePostDto 로 변환 테스트 - 비어있는 경우 null로 변환`() {

        //given
        val content = ""
        val title = "example title"

        val createPostRequest = updatePostRequest(content = content, title = title)
        val upd = updatePostDto(content = null, title = title)

        //when
        val toServiceDto = createPostRequest.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(upd)
    }





    @Test
    fun `UpdatePostRequest에서 UpdatePostDto 로 변환 테스트 - 공백만 있는 경우 null로 변환`() {

        //given
        val content = "             "
        val title = "example title"

        val createPostRequest = updatePostRequest(content = content, title = title)
        val upd = updatePostDto(content = null, title = title)

        //when
        val toServiceDto = createPostRequest.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(upd)
    }





    @Test
    fun `UpdatePostRequest에서 UpdatePostDto 로 변환 테스트 - null인 경우 null로 변환`() {

        //given
        val content = null
        val title = "example title"

        val createPostRequest = updatePostRequest(content = content, title = title)
        val upd = updatePostDto(content = null, title = title)

        //when
        val toServiceDto = createPostRequest.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(upd)
    }
}