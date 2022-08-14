package com.whyrano.domain.member.service

import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.dto.CreateMemberDto
import com.whyrano.domain.member.service.dto.UpdateMemberDto
import com.whyrano.global.auth.exception.AuthException
import com.whyrano.global.auth.exception.AuthExceptionType
import com.whyrano.global.auth.userdetails.AuthMember
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by ShinD on 2022/08/09.
 */
@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {


    /**
     * 회원 가입
     */
    fun signUp(cmd: CreateMemberDto): Long {


        // 아이디 중복 체크
        memberRepository.findByEmail(cmd.email)?.let { throw MemberException(MemberExceptionType.ALREADY_EXIST) } // 이미 가입된 이메일인 경우 예외 발생

        return memberRepository.save(cmd.toEntity(passwordEncoder)).id!!
    }



    /**
     * 회원 정보 수정
     */
    fun update(id: Long, umd: UpdateMemberDto) {
        memberRepository.findByIdOrNull(id) // 아이디를 통해 회원 정보 조회
            ?.apply { update(umd.nickname, umd.encodedPassword(passwordEncoder), umd.profileImagePath) }  // 존재하는 경우 회원 정보 수정
            ?: throw MemberException(MemberExceptionType.NOT_FOUND) // 존재하지 않는 경우 예외 발생
    }

    /**
     * 회원 탈퇴
     */
    fun delete(id: Long, password: String) {

        // 회원 정보가 존재하지 않는 경우 예외 발생
        val findMember = memberRepository.findByIdOrNull(id) ?: throw MemberException(MemberExceptionType.NOT_FOUND)

        // 비밀번호 일치 여부 체크
        if (passwordEncoder.matches(password, findMember.password)) {
            memberRepository.delete(findMember) // 일치한 경우 삭제
        }
        else throw MemberException(MemberExceptionType.UNMATCHED_PASSWORD) // 일치하지 않는 경우 예외 발생
    }


    override fun loadUserByUsername(username: String): UserDetails {
        val member = memberRepository.findByEmail(username) ?: throw AuthException(AuthExceptionType.NOT_FOUND_MEMBER)

        return AuthMember(id = member.id!!, email = member.email, password = member.password, role = member.role)
    }
}