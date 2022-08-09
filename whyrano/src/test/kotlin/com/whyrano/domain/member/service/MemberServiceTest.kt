package com.whyrano.domain.member.service

import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.fixture.MemberFixture.EMAIL
import com.whyrano.domain.member.fixture.MemberFixture.createMemberDto
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.repository.MemberRepository
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Created by ShinD on 2022/08/09.
 */
@ExtendWith(SpringExtension::class)
internal class MemberServiceTest{

    @MockkBean
    private lateinit var memberRepository: MemberRepository
    private lateinit var memberService: MemberService



    @BeforeEach
    fun setUp() {
        memberService = MemberService(memberRepository)
    }




    @Test
    @DisplayName("회원 가입 성공")
    fun test_signup_success() {
        //given
        val createMemberDto = createMemberDto()
        every { memberRepository.findByEmail(createMemberDto.email) } returns null
        every { memberRepository.save(any()) } returns member()

        //when
        memberService.signUp(createMemberDto)

        //then
        verify { memberRepository.save(any()) }
    }

    @Test
    @DisplayName("회원 가입 실패 : 아이디 중복")
    fun test_signup_fail_by_duplicated_email() {
        //given
        val createMemberDto = createMemberDto(email = EMAIL)
        every { memberRepository.findByEmail(createMemberDto.email) } returns member()


        //when
        Assertions.assertThrows(IllegalStateException::class.java){memberService.signUp(createMemberDto)}


        //then
        verify (exactly = 1){ memberRepository.findByEmail(any()) }
        verify (exactly = 0){ memberRepository.save(any()) }
    }

}

