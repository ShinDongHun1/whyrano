package com.whyrano.domain.member.service.dto

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Created by ShinD on 2022/08/09.
 */
data class CreateMemberDto(
    var role: Role = Role.BASIC,
    var email: String,
    var password: String,
    var nickname: String,
    var profileImagePath: String?,
) {
    fun toEntity(passwordEncoder: PasswordEncoder): Member {
        return Member(
            role = role,
            email = email,
            password = passwordEncoder.encode(password),
            nickname = nickname,
            profileImagePath = profileImagePath,
        )
    }
}