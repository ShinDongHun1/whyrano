package com.whyrano.domain.member.controller.response

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.domain.member.fixture.MemberFixture.memberDto
import com.whyrano.domain.member.fixture.MemberFixture.memberInfoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Created by ShinD on 2022/08/17.
 */
internal class MemberInfoResponseTest {

    companion object {
        private val objectMapper = ObjectMapper()
        private val JSON_FORMAT = """
            {
            "id":%s,
            "role":"%s",
            "email":"%s",
            "nickname":"%s",
            "point":%s,
            "profileImagePath":"%s",
            "createdDate":"%s",
            "modifiedDate":"%s"
            }
        """.trimIndent()
    }


    @Test
    fun `MemberDto 에서 MemberInfoResponse 변환`() {

        //given
        val memberDto = memberDto()

        //when
        val from = MemberInfoResponse.from(memberDto)

        //then
        val memberInfoResponse = memberInfoResponse(createdDate = memberDto.createdDate, modifiedDate = memberDto.modifiedDate)
        assertThat(from).isEqualTo(memberInfoResponse)
    }





    @Test
    fun `MemberInfoResponse 에서 JSON 변환`() {

        //given
        val mir = memberInfoResponse()

        //when
        val writeValueAsString = objectMapper.writeValueAsString(mir)

        //then
        assertThat(writeValueAsString).isEqualTo(JSON_FORMAT.format(mir.id, mir.role, mir.email, mir.nickname, mir.point, mir.profileImagePath, mir.createdDate, mir.modifiedDate)
                                                            .replace("\t","").replace(" ","").replace("\n", ""))
    }
}