package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.config.SecurityConfig.Companion.LOGIN_URL
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ShinD on 2022/08/09.
 */

/**
 * 확실히 얘가 빠르다
 */
@WebMvcTest
@Import(SecurityConfig::class, MemberService::class)
internal class JsonLoginProcessingFilterTestUsingWebMvcTest {
    
    companion object {
        private val objectMapper = ObjectMapper()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc
    @MockkBean
    private lateinit var memberRepository: MemberRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder







    @Test
    @DisplayName("로그인 get 요청인 경우 - 401")
    fun test_login_get_fail_unauthorized() {
        mockMvc
            .perform(
                     get(LOGIN_URL)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("로그인시 json이 아닌 경우 - 401")
    fun test_login_noJson_fail_unauthorized() {
        mockMvc
            .perform(
                    post(LOGIN_URL)
                    .contentType(APPLICATION_FORM_URLENCODED)
            )
            .andExpect(status().isUnauthorized)
    }



    @Test
    @DisplayName("로그인 시 내용이 없는 경우- 401")
    fun test_login_fail_noContent_unauthorized() {
        mockMvc
            .perform(
                post(LOGIN_URL)
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("로그인 시 아이디가 일치하지 않는 경우- 401")
    fun test_login_fail_notMatchUsername_unauthorized() {

        val member = MemberFixture.createMemberDto()
        every { memberRepository.findByEmail(member.email) } returns null

        val hashMap = usernamePasswordHashMap(member.email, member.password)
        mockMvc
            .perform(
                post(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(
                   objectMapper.writeValueAsString(hashMap)
                )
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("로그인 시 비밀번호가 일치하지 않는 경우- 401")
    fun test_login_fail_notMatchPassword_unauthorized() {

        val member =  MemberFixture.createMemberDto()
        every { memberRepository.findByEmail(member.email) } returns member.toEntity(passwordEncoder)

        val noMatchPassword = member.password + "12345"

        val hashMap = usernamePasswordHashMap(member.email, noMatchPassword)


        mockMvc
            .perform(
                post(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(
                   objectMapper.writeValueAsString(hashMap)
                )
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("로그인 성공")
    fun test_login_success_notMatchPassword_unauthorized() {

        val member = MemberFixture.createMemberDto()
        every { memberRepository.findByEmail(member.email) } returns member.toEntity(passwordEncoder)


        val hashMap = usernamePasswordHashMap(member.email, member.password)


        val result = mockMvc
            .perform(
                post(LOGIN_URL)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hashMap))
            ).andReturn()

        assertThat( HttpStatus.valueOf(result.response.status).is4xxClientError ).isFalse
    }


    private fun usernamePasswordHashMap(username: String, passwrod: String): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["username"] = username
        hashMap["password"] = passwrod
        return hashMap
    }
}

