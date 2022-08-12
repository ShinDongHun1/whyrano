package com.whyrano.domain.common.auditoraware

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.repository.MemberRepository
import org.springframework.data.domain.AuditorAware
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

/**
 * Created by ShinD on 2022/08/11.
 */
class MemberAuditorAware(
    private val memberRepository: MemberRepository
) : AuditorAware<Member> {

    override fun getCurrentAuditor(): Optional<Member> =
        Optional.ofNullable(SecurityContextHolder.getContext())
            .map { it.authentication }
            .filter{ it.isAuthenticated }
            .map { memberRepository.findByEmail(it.name) }
}