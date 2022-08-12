package com.whyrano.global.auth.userdetails

import com.whyrano.domain.member.entity.Role
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User

/**
 * Created by ShinD on 2022/08/13.
 */
class AuthMember(
    val id: Long,
    val email: String,
    password: String = "SECRET",
    val role: Role,
) : User(email, password, listOf(SimpleGrantedAuthority(role.authority)))//SimpleGrantedAuthority(ROLE_XXX)) 이런 식으로 사용함