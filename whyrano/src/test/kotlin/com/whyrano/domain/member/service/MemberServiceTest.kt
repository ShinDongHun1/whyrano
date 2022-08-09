package com.whyrano.domain.member.service

import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.EMAIL
import com.whyrano.domain.member.fixture.MemberFixture.createMemberDto
import com.whyrano.domain.member.repository.MemberRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.security.crypto.factory.PasswordEncoderFactories

/**
 * Created by ShinD on 2022/08/09.
 */
@DataJpaTest
internal class MemberServiceTest{

    @Autowired
    private lateinit var memberRepository: MemberRepository

    private lateinit var memberService: MemberService

    private val passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()


    @BeforeEach
    fun setUp() {
        memberService = MemberService(memberRepository, passwordEncoder)
    }



    @Test
    @DisplayName("회원 가입 성공")
    fun test_signup_success() {
        //given
        val createMemberDto = createMemberDto()

        //when
        memberService.signUp(createMemberDto)

        //then
        val findMember = memberRepository.findByEmail(createMemberDto.email)
        assertThat(findMember?.id).isNotNull
        assertThat(findMember!!.nickname).isEqualTo(createMemberDto.nickname)
    }


    @Test
    @DisplayName("회원 가입 시 비밀번호 인코딩 성공")
    fun test_signUp_password_encoding() {
        //given
        val createMemberDto = createMemberDto()

        //when
        memberService.signUp(createMemberDto)

        //then
        val findMember = memberRepository.findByEmail(createMemberDto.email)
        assertThat(findMember!!.password).isNotEqualTo(createMemberDto.password)
        assertThat( passwordEncoder.matches(createMemberDto.password, findMember.password) ).isTrue
    }


    @Test
    @DisplayName("회원 가입 실패 : 아이디 중복")
    fun test_signup_fail_cause_duplicated_email() {
        //given
        val createMemberDto = createMemberDto(email = EMAIL)
        memberService.signUp(createMemberDto)

        //when, then
        assertThrows(IllegalStateException::class.java) { memberService.signUp(createMemberDto) }
    }


    @Test
    @DisplayName("회원 수정 성공")
    fun test_update_success() {
        //given
        val createMemberDto = createMemberDto()
        memberService.signUp(createMemberDto)
        val member = memberRepository.findByEmail(createMemberDto.email)!!


        //when
        val updateMemberDto = MemberFixture.updateMemberDto()
        memberService.update(member.id!!, updateMemberDto)


        //then
        val findMember = memberRepository.findByEmail(createMemberDto.email)
        assertThat(findMember!!.nickname).isEqualTo(updateMemberDto.nickname)
        assertThat(findMember.profileImagePath).isEqualTo(updateMemberDto.profileImagePath)

        assertThat( passwordEncoder.matches(createMemberDto.password, findMember.password) ).isFalse
        assertThat( passwordEncoder.matches(updateMemberDto.password, findMember.password) ).isTrue
    }

    @Test
    @DisplayName("회원 수정시 비밀번호 인코딩")
    fun test_update_encode_password() {
        //given
        val createMemberDto = createMemberDto()
        memberService.signUp(createMemberDto)
        val member = memberRepository.findByEmail(createMemberDto.email)!!


        //when
        val updateMemberDto = MemberFixture.updateMemberDto(password = "UPDATE!!!!!")
        memberService.update(member.id!!, updateMemberDto)


        //then
        val findMember = memberRepository.findByEmail(createMemberDto.email)!!

        assertThat( passwordEncoder.matches(createMemberDto.password, findMember.password) ).isFalse
        assertThat( passwordEncoder.matches(updateMemberDto.password, findMember.password) ).isTrue
    }


    @Test
    @DisplayName("회원 수정 실패 - 없는 회원")
    fun test_update_fail_cause_no_exist_member() {
        //given
        val updateMemberDto = MemberFixture.updateMemberDto()
        val noExistId = 1L

        //when
        assertThrows(IllegalStateException::class.java) { memberService.update(noExistId, updateMemberDto) }
    }

}

