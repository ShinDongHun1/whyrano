package com.whyrano.domain.member.repository

import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/09.
 */
interface MemberRepository : JpaRepository<Member, Long> {

    fun findByAccessTokenAndRefreshToken(accessToken: AccessToken, refreshToken: RefreshToken): Member?

    fun findByEmail(email: String): Member?

}