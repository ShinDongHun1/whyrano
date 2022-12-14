package com.whyrano.global.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.controller.MemberController
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.accessToken
import com.whyrano.domain.member.fixture.MemberFixture.authMember
import com.whyrano.domain.member.fixture.MemberFixture.refreshToken
import com.whyrano.domain.member.service.MemberService
import com.whyrano.domain.post.controller.PostController
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.config.PermitAllURI
import com.whyrano.global.config.SecurityConfig
import com.whyrano.global.exception.ExceptionResponse
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ShinD on 2022/08/09.
 */

/**
 * ????????? ?????? ?????????
 */
@WebMvcTest(
    excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [PostController::class, MemberController::class])]
)
@Import(SecurityConfig::class)
internal class JsonLoginProcessingFilterTestUsingWebMvcTest {
    
    companion object {
        private val objectMapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
        private var passwordEncoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var memberService: MemberService

    @MockkBean
    private lateinit var jwtService: JwtService







    @Test
    @DisplayName("????????? get ????????? ?????? - 405")
    fun test_login_get_fail_unauthorized() {
        every { jwtService.extractToken(any()) } returns TokenDto()
        val result = mockMvc
            .perform(
                get(PermitAllURI.URI.LOGIN_URI.uri)
                    .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isMethodNotAllowed)
            .andReturn()

        val readValue = objectMapper.readValue(result.response.contentAsString, ExceptionResponse::class.java)
        assertThat(readValue.errorCode).isEqualTo(1100)

    }


    @Test
    @DisplayName("???????????? json??? ?????? ?????? - 415")
    fun test_login_noJson_fail_unauthorized() {
        mockMvc
            .perform(
                    post(PermitAllURI.URI.LOGIN_URI.uri)
                    .contentType(APPLICATION_FORM_URLENCODED)
            )
            .andExpect(status().isUnsupportedMediaType)
    }



    @Test
    @DisplayName("????????? ??? ????????? ?????? ??????- 401")
    fun test_login_fail_noContent_unauthorized() {
        mockMvc
            .perform(
                post(PermitAllURI.URI.LOGIN_URI.uri)
                .contentType(APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("????????? ??? ???????????? ???????????? ?????? ??????- 401")
    fun test_login_fail_notMatchUsername_unauthorized() {

        val member = MemberFixture.createMemberDto()
        every { memberService.loadUserByUsername(any()) }.throws(UsernameNotFoundException("D"))

        val hashMap = usernamePasswordHashMap(member.email, member.password)
        mockMvc
            .perform(
                post(PermitAllURI.URI.LOGIN_URI.uri)
                .contentType(APPLICATION_JSON)
                .content(
                   objectMapper.writeValueAsString(hashMap)
                )
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("????????? ??? ??????????????? ???????????? ?????? ??????- 401")
    fun test_login_fail_notMatchPassword_unauthorized() {

        val member =  MemberFixture.createMemberDto()
        every { memberService.loadUserByUsername(any()) }.throws(UsernameNotFoundException("D"))

        val noMatchPassword = member.password + "12345"

        val hashMap = usernamePasswordHashMap(member.email, noMatchPassword)


        mockMvc
            .perform(
                post(PermitAllURI.URI.LOGIN_URI.uri)
                .contentType(APPLICATION_JSON)
                .content(
                   objectMapper.writeValueAsString(hashMap)
                )
            )
            .andExpect(status().isUnauthorized)
    }


    @Test
    @DisplayName("????????? ??????")
    fun test_login_success_notMatchPassword_unauthorized() {

        val memberDto = MemberFixture.createMemberDto()
        val member = memberDto.toEntity(passwordEncoder)
        every { memberService.loadUserByUsername(member.email) } returns authMember(password = member.password)


        every { jwtService.createAccessAndRefreshToken(any()) } returns TokenDto(accessToken().accessToken, refreshToken().refreshToken)


        val hashMap = usernamePasswordHashMap(memberDto.email, memberDto.password)


        val result = mockMvc
            .perform(
                post(PermitAllURI.URI.LOGIN_URI.uri)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(hashMap))
            ).andReturn()

        assertThat( HttpStatus.valueOf(result.response.status).is4xxClientError ).isFalse
    }


    private fun usernamePasswordHashMap(username: String, password: String): HashMap<String, String> {
        val hashMap = HashMap<String, String>()
        hashMap["username"] = username
        hashMap["password"] = password
        return hashMap
    }
}

