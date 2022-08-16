package com.whyrano.global.auth.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.controller.MemberController
import com.whyrano.domain.member.fixture.MemberFixture.authMember
import com.whyrano.domain.member.fixture.MemberFixture.createMemberDto
import com.whyrano.domain.member.service.MemberService
import com.whyrano.domain.post.controller.PostController
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.config.SecurityConfig.Companion.LOGIN_URL
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ShinD on 2022/08/10.
 */
@WebMvcTest(
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [PostController::class, MemberController::class])]
)
@Import(SecurityConfig::class)
internal class JsonLoginSuccessHandlerTest {
    companion object {
        private val objectMapper = ObjectMapper()
        private var passwordEncoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }


    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberService: MemberService

    @MockkBean
    private lateinit var jwtService: JwtService


    @Test
    @DisplayName("로그인 성공시 JWT 발급, 상태코드 200")
    fun test_login_success_publish_jwt() {
        //given
        val memberDto = createMemberDto()
        val member = memberDto.toEntity(passwordEncoder)
        every { memberService.loadUserByUsername(member.email) } returns authMember(password = member.password)
        every { jwtService.createAccessAndRefreshToken(any()) } returns TokenDto("Access", "ref")

        val hashMap = usernamePasswordHashMap(memberDto.email, memberDto.password)


        //when
        val result = mockMvc
            .perform(
                post(LOGIN_URL)
                    .contentType(APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(hashMap)
                    )
            )
            .andExpect(status().isOk)
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