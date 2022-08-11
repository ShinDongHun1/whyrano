//package com.whyrano.global.auth.filter
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.whyrano.domain.member.fixture.MemberFixture
//import com.whyrano.domain.member.service.MemberService
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
//import org.springframework.http.MediaType.APPLICATION_JSON
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import org.springframework.transaction.annotation.Transactional
//
///**
// * Created by ShinD on 2022/08/09.
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@Transactional
//internal class JsonLoginProcessingFilterTestUsingAutoConfigureMockMvc {
//
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//    @Autowired
//    private lateinit var memberService: MemberService
//
//
//
//
//
//
//
//
//
//    @Test
//    @DisplayName("로그인 get 요청인 경우 - 401")
//    fun test_login_get_fail_unauthorized() {
//        mockMvc
//            .perform(get("/login").contentType(APPLICATION_JSON))
//            .andExpect(status().isUnauthorized)
//    }
//
//    @Test
//    @DisplayName("로그인시 json이 아닌 경우 - 401")
//    fun test_login_noJson_fail_unauthorized() {
//        mockMvc
//            .perform(post("/login").contentType(APPLICATION_FORM_URLENCODED))
//            .andExpect(status().isUnauthorized)
//    }
//
//    @Test
//    @DisplayName("로그인 시 내용이 없는 경우- 401")
//    fun test_login_fail_noContent_unauthorized() {
//        mockMvc
//            .perform(
//                post("/login")
//                .contentType(APPLICATION_JSON)
//            )
//            .andExpect(status().isUnauthorized)
//    }
//
//    @Test
//    @DisplayName("로그인 시 아이디가 일치하지 않는 경우- 401")
//    fun test_login_fail_notMatchUsername_unauthorized() {
//
//        val member = MemberFixture.createMemberDto()
//        memberService.signUp(member)
//
//        val hashMap = HashMap<String, String>()
//        hashMap.put("username", member.email+"!1")
//        hashMap.put("password", member.password )
//        mockMvc
//            .perform(
//                post("/login")
//                .contentType(APPLICATION_JSON)
//                .content(
//                    ObjectMapper().writeValueAsString(hashMap)
//                )
//            )
//            .andExpect(status().isUnauthorized)
//    }
//
//
//    @Test
//    @DisplayName("로그인 시 비밀번호가 일치하지 않는 경우- 401")
//    fun test_login_fail_notMatchPassword_unauthorized() {
//
//        val member =  MemberFixture.createMemberDto()
//        memberService.signUp(member)
//
//        val hashMap = HashMap<String, String>()
//        hashMap.put("username", member.email)
//        hashMap.put("password", member.password + "11")
//        mockMvc
//            .perform(
//                post("/login")
//                .contentType(APPLICATION_JSON)
//                .content(
//                    ObjectMapper().writeValueAsString(hashMap)
//                )
//            )
//            .andExpect(status().isUnauthorized)
//    }
//
//
//    @Test
//    @DisplayName("로그인 성공")
//    fun test_login_success_notMatchPassword_unauthorized() {
//
//        val member = MemberFixture.createMemberDto()
//        memberService.signUp(member)
//
//        val hashMap = HashMap<String, String>()
//        hashMap.put("username", member.email)
//        hashMap.put("password", member.password)
//
//        mockMvc
//            .perform(
//                post("/login")
//                .contentType(APPLICATION_JSON)
//                .content(
//                    ObjectMapper().writeValueAsString(hashMap)
//                )
//            )
//            .andExpect(status().isOk)
//    }
//}
//
