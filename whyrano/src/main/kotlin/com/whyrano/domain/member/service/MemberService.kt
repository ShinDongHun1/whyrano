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

    fun signUp(createMemberDto: CreateMemberDto) : Long {
        memberRepository.findByEmail(createMemberDto.email)
            ?.let { throw MemberException(MemberExceptionType.ALREADY_EXIST) } // 이미 가입된 이메일인 경우 예외 발생

        return memberRepository.save(createMemberDto.toEntity(passwordEncoder)).id!!
    }

    fun update(id: Long, UMDto: UpdateMemberDto) {
        memberRepository.findByIdOrNull(id)
            ?.apply { update(UMDto.nickname, UMDto.encodedPassword(passwordEncoder), UMDto.profileImagePath) }  // 존재하는 경우 update
            ?: throw MemberException(MemberExceptionType.NOT_FOUND) // 존재하지 않는 경우 예외 발생
    }

    fun delete(id: Long, password: String) {
        val findMember = memberRepository.findByIdOrNull(id)
            ?: throw MemberException(MemberExceptionType.NOT_FOUND) // 존재하지 않는 경우 예외 발생

        if (passwordEncoder.matches(password, findMember.password)) {
            memberRepository.delete(findMember)
        }
        else throw MemberException(MemberExceptionType.UNMATCHED_PASSWORD)
    }


    override fun loadUserByUsername(username: String): UserDetails {
        val member = memberRepository.findByEmail(username) ?: throw AuthException(AuthExceptionType.NOT_FOUND_MEMBER)

        return AuthMember(id = member.id!!, email = member.email, password = member.password, role = member.role)
    }


}