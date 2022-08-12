package com.whyrano.domain.member.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture.createMemberDto
import com.whyrano.domain.member.fixture.MemberFixture.createMemberRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Created by ShinD on 2022/08/12.
 */
internal class CreateMemberRequestTest {

    companion object {
        private val objectMapper = ObjectMapper()
        private var JSON_STRING_FORMAT = """
            {   
                "email":"%s",
                "password":"%s",
                "nickname":"%s",
                "profileImagePath":"%s"
            }
        """.replace("\t","").replace("\n","").trim()
    }

    @Test
    fun `Json - CreateMemberRequestTest 변환 테스트`() {
        //given
        val cmr = createMemberRequest()
        val json = JSON_STRING_FORMAT.format(cmr.email, cmr.password, cmr.nickname, cmr.profileImagePath)


        //when
        val readValue = objectMapper.readValue(json, CreateMemberRequest::class.java)

        //then
        assertThat(cmr).isEqualTo(readValue)
    }



    @Test
    fun `Json - CreateMemberRequestTest 변환 테스트 - profile 이미지가 없는 경우`() {
        //given
        val cmr = createMemberRequest(profileImagePath = "")//null이 아닌 ""이다
        val json = JSON_STRING_FORMAT.format(cmr.email, cmr.password, cmr.nickname, "")


        //when
        val readValue = objectMapper.readValue(json, CreateMemberRequest::class.java)

        //then
        assertThat(cmr).isEqualTo(readValue)
    }


    @Test
    fun `CreateMemberRequestTest - CreateMemberRequestDto 변환 테스트`() {
        //given
        val cmr = createMemberRequest()
        val createMemberDto = createMemberDto(Role.BASIC, cmr.email!!, cmr.password!!, cmr.nickname!!, cmr.profileImagePath)

        //when
        val toServiceDto = cmr.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(createMemberDto)
    }

    @Test
    fun `CreateMemberRequestTest - CreateMemberRequestDto 변환 테스트 - profileImagePath가 Null인 경우`() {
        //given
        val cmr = createMemberRequest(profileImagePath = null)
        val createMemberDto = createMemberDto(Role.BASIC, cmr.email!!, cmr.password!!, cmr.nickname!!, null)

        //when
        val toServiceDto = cmr.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(createMemberDto)
    }

    @Test
    fun `CreateMemberRequestTest - CreateMemberRequestDto 변환 테스트 - profileImagePath가 ""인 경우`() {
        //given
        val cmr = createMemberRequest(profileImagePath = "")

        //""인 경우 null로 바뀌어야 함
        val createMemberDto = createMemberDto(Role.BASIC, cmr.email!!, cmr.password!!, cmr.nickname!!, null)

        //when
        val toServiceDto = cmr.toServiceDto()

        //then
        assertThat(toServiceDto).isEqualTo(createMemberDto)
    }
}