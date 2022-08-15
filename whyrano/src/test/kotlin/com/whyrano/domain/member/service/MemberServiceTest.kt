package com.whyrano.domain.member.service

import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.EMAIL
import com.whyrano.domain.member.fixture.MemberFixture.createMemberDto
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.dto.CreateMemberDto
import com.whyrano.domain.post.repository.QueryPostRepository
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
@MockkBean(QueryPostRepository::class)
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
    @DisplayName("회원 가입 시 비밀번호 암호화 성공")
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
        val exceptionType = assertThrows(MemberException::class.java) { memberService.signUp(createMemberDto) }
            .exceptionType()
        assertThat(exceptionType).isEqualTo(MemberExceptionType.ALREADY_EXIST)
    }


    @Test
    @DisplayName("회원 수정 성공")
    fun test_update_success() {
        //given
        val createMemberDto = createMemberDto()
        val member = createMember(createMemberDto)


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
    @DisplayName("회원 수정시 비밀번호 암호화")
    fun test_update_encode_password() {
        //given
        val createMemberDto = createMemberDto()
        val member = createMember(createMemberDto)


        //when
        val updateMemberDto = MemberFixture.updateMemberDto(password = "UPDATE!!!!!")
        memberService.update(member.id!!, updateMemberDto)


        //then
        val findMember = memberRepository.findByEmail(createMemberDto.email)!!

        assertThat( passwordEncoder.matches(createMemberDto.password, findMember.password) ).isFalse
        assertThat( passwordEncoder.matches(updateMemberDto.password, findMember.password) ).isTrue
    }

    private fun createMember(createMemberDto: CreateMemberDto): Member {
        memberService.signUp(createMemberDto)
        return memberRepository.findByEmail(createMemberDto.email)!!
    }

    @Test
    @DisplayName("회원 수정 실패 - 없는 회원")
    fun test_update_fail_cause_no_exist_member() {
        //given
        val updateMemberDto = MemberFixture.updateMemberDto()
        val noExistId = 1L

        //when
        val exceptionType = assertThrows(MemberException::class.java) { memberService.update(noExistId, updateMemberDto) }
                .exceptionType()

        assertThat(exceptionType).isEqualTo(MemberExceptionType.NOT_FOUND)
    }

    @Test
    @DisplayName("회원 삭제 성공")
    fun test_delete_success() {
        //given
        val createMemberDto = createMemberDto()
        val member = createMember(createMemberDto)

        //when
        memberService.delete(member.id!!, createMemberDto.password)

        //then
        assertThat(memberRepository.findByEmail(member.email)).isNull()

    }

    @Test
    @DisplayName("회원 삭제 실패 - 비밀번호 불일치")
    fun test_delete_fail_cause_unMatchPassword() {
        //given
        val createMemberDto = createMemberDto()
        val member = createMember(createMemberDto)

        //when, then
        val exceptionType = assertThrows(MemberException::class.java) {
            memberService.delete(member.id!!, createMemberDto.password + "!!!!!")
        }.exceptionType()

        assertThat(exceptionType).isEqualTo(MemberExceptionType.UNMATCHED_PASSWORD)

    }
}

