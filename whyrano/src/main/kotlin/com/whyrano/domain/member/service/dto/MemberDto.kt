package com.whyrano.domain.member.service.dto

import com.whyrano.domain.member.entity.Role

/**
 * Created by ShinD on 2022/08/15.
 */
data class MemberDto(
    val id: Long,

    var role: Role, // 권한

    var email: String, // 이메일

    var nickname: String, // 닉네임

    var point: Int = 0, // 포인트

    var profileImagePath: String?, // 프로필 사진 경로 (https://~~)
)