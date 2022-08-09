package com.whyrano.domain.member.service.dto

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import javax.persistence.Column

/**
 * Created by ShinD on 2022/08/09.
 */
class CreateMemberDto(
    var authority: Role = Role.BASIC,
    var email: String,
    var password: String,
    var nickname: String,
    var profileImagePath: String,
) {
    fun toEntity(): Member {
        return Member(
            authority = authority,
            email = email,
            password = password,
            nickname = nickname,
            profileImagePath = profileImagePath,
        )
    }
}