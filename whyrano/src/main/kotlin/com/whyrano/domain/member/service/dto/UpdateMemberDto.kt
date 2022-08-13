package com.whyrano.domain.member.service.dto

import org.springframework.security.crypto.password.PasswordEncoder

/**
 * Created by ShinD on 2022/08/09.
 */
data class UpdateMemberDto (
    var password: String? = null,
    var nickname: String? = null,
    var profileImagePath: String? = null,
) {
    fun encodedPassword(passwordEncoder: PasswordEncoder): String? =
        password?.let { passwordEncoder.encode(it) }

}