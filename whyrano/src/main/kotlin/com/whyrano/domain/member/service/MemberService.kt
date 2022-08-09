package com.whyrano.domain.member.service

import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.member.service.dto.CreateMemberDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.IllegalStateException

/**
 * Created by ShinD on 2022/08/09.
 */
@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
) {

    fun signUp(createMemberDto: CreateMemberDto) {

        memberRepository.findByEmail(createMemberDto.email)
            ?.let { throw IllegalStateException("이미 존재") } // 이미 가입된 이메일인 경우 예외 발생

        memberRepository.save(createMemberDto.toEntity())
    }

}