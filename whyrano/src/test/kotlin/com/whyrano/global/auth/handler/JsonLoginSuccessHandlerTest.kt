package com.whyrano.global.auth.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.jwt.JwtServiceImpl
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.JwtConfig
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.config.SecurityConfig.Companion.LOGIN_URL
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

/**
 * Created by ShinD on 2022/08/10.
 */
@WebMvcTest
@Import(SecurityConfig::class, MemberService::class, JwtServiceImpl::class, JwtConfig::class)
@ActiveProfiles("local")
internal class JsonLoginSuccessHandlerTest {
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
    @DisplayName("로그인 성공시 JWT 발급, 상태코드 200")
    fun test_login_success_publish_jwt() {
        //given
        val member = MemberFixture.createMemberDto()
        every { memberRepository.findByEmail(member.email) } returns member.toEntity(passwordEncoder)

        val hashMap = usernamePasswordHashMap(member.email, member.password)


        //when
        val result = mockMvc
            .perform(
                MockMvcRequestBuilders.post(LOGIN_URL)
                    .contentType(APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(hashMap)
                    )
            )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()


        //then
        val tokens = objectMapper.readValue(result.response.contentAsString, TokenDto::class.java)


        assertThat(tokens.refreshToken()).isNotNull
        assertThat(tokens.accessToken()).isNotNull

    }



    private fun usernamePasswordHashMap(username: String, passwrod: String): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["username"] = username
        hashMap["password"] = passwrod
        return hashMap
    }
}