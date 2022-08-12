package com.whyrano.global.auth.userdetails

import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * Created by ShinD on 2022/08/13.
 */
internal class AuthMemberTest {

    @Test
    fun `AuthMember의 authories 확인하기`() {
        val authMember = MemberFixture.authMember(role = Role.BLACK)
        assertThat(authMember.authorities).containsOnly(SimpleGrantedAuthority(Role.BLACK.authority))
    }

    @Test
    fun `AuthMember에서 UserDetail로 변환했을때, username등이 모두 동일하게 동작하는지 확인`() {
        val authMember = MemberFixture.authMember(role = Role.BLACK)
        val userDetails: UserDetails = authMember


        assertThat(authMember.authorities).containsAll(userDetails.authorities)
        assertThat(authMember.username).isEqualTo(userDetails.username)
        assertThat(authMember.email).isEqualTo(userDetails.username)
        assertThat(authMember.password).isEqualTo(userDetails.password)
    }
}