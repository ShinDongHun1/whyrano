package com.whyrano.global.config


import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Created by ShinD on 2022/08/09.
 */
@WebMvcTest
@Import(SecurityConfig::class)
class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc




    @Test
    @DisplayName("회원가입 권한 없이 가능")
    fun test_signup_permitAll() {
        mockMvc
            .perform(get("/signup"))
            .andExpect(status().isNotFound)
    }


    @Test
    @DisplayName("로그인하지 않은 유저의 일반 경로 접속")
    fun test_noLogin_to_basic() {
        mockMvc
            .perform(get("/basic"))
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("로그인하지 않은 유저의 어드민 경로 접속")
    fun test_noLogin_to_admin() {
        mockMvc
            .perform(get("/admin"))
            .andExpect(status().isForbidden)
    }


    @Test
    @DisplayName("일반(BASIC) 유저의 일반 경로 접속")
    @WithMockUser(roles = ["BASIC"])
    fun test_basic_to_basic() {
        mockMvc
            .perform(get("/test_authenticated"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("어드민(ADMIN) 유저의 일반 경로 접속")
    @WithMockUser(roles = ["ADMIN"])
    fun test_admin_to_basic() {
        mockMvc
            .perform(get("/test_authenticated"))
            .andExpect(status().isNotFound)
    }

    @Test
    @DisplayName("일반(BASIC) 유저의 관리자 경로 접속")
    @WithMockUser(roles = ["BASIC"])
    fun test_basic_to_admin() {
        mockMvc
            .perform(get("/admin"))
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("어드민(ADMIN) 유저의 관리자 경로 접속")
    @WithMockUser(roles = ["ADMIN"])
    fun test_admin_to_admin() {
        mockMvc
            .perform(get("/admin"))
            .andExpect(status().isNotFound)
    }
}


