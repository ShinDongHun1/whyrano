package com.whyrano.domain.member.service.dto

/**
 * Created by ShinD on 2022/08/09.
 */
data class UpdateMemberDto (
    var password: String? = null,
    var nickname: String? = null,
    var profileImagePath: String? = null,
)