package com.whyrano.domain.member.repository

import com.whyrano.domain.member.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/09.
 */
interface MemberRepository : JpaRepository<Member, Long> {

    fun findByEmail(email: String): Member?

}