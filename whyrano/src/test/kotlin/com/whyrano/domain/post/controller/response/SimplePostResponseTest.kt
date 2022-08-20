package com.whyrano.domain.post.controller.response

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.memberInfoResponse
import com.whyrano.domain.post.fixture.PostFixture
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Created by ShinD on 2022/08/17.
 */
internal class SimplePostResponseTest {


    companion object {

        private val objectMapper = ObjectMapper()
        private val JSON_FORMAT = """
        {
            "id":%s,
            "postType":"%s",
            "title":"%s",
            "content":"%s",
            "answerCount":%s,
            "viewCount":%s,
            "likeCount":%s,
            "commentCount":%s,
            "createdDate":"%s",
            "modifiedDate":"%s",
            "writerInfo" : { "id":%s,
                             "role":"%s",
                             "email":"%s",
                             "nickname":"%s",
                             "point":%s,
                             "profileImagePath":"%s",
                             "createdDate":"%s",
                             "modifiedDate":"%s"
                            }
        }
        """.trimIndent()
    }



    @Test
    fun `SimplePostDto 에서 SimplePostResponse로 변환`() {

        //given
        val md = MemberFixture.memberDto()
        val spd = PostFixture.simplePostDto(writerDto = md)

        //when
        val from = SimplePostResponse.from(spd)

        //then
        val simplePostResponse = PostFixture.simplePostResponse(
            createdDate = spd.createdDate,
            modifiedDate = spd.modifiedDate,
            writerInfoResponse = memberInfoResponse(createdDate = md.createdDate, modifiedDate = md.modifiedDate)
        )
        Assertions.assertThat(from).isEqualTo(simplePostResponse)
    }



    @Test
    fun `SimplePostResponse로 에서 JSON 변환`() {

        //given
        val spr = PostFixture.simplePostResponse()

        //when
        val writeValueAsString = objectMapper.writeValueAsString(spr)

        //then
        assertThat(writeValueAsString).isEqualTo(
            JSON_FORMAT.replace("\t", "")
                .replace(" ", "")
                .replace("\n", "")
                .format(
                    spr.id, spr.postType, spr.title, spr.content,
                    spr.answerCount, spr.viewCount, spr.likeCount,
                    spr.commentCount, spr.createdDate, spr.modifiedDate,
                    spr.writerInfo.id, spr.writerInfo.role, spr.writerInfo.email,
                    spr.writerInfo.nickname, spr.writerInfo.point, spr.writerInfo.profileImagePath,
                    spr.writerInfo.createdDate, spr.writerInfo.modifiedDate
                )
        )
    }
}