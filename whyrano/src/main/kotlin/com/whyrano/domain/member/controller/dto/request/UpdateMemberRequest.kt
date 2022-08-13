package com.whyrano.domain.member.controller.dto.request

import com.whyrano.domain.member.service.dto.UpdateMemberDto

/**
 * Created by ShinD on 2022/08/13.
 */
data class UpdateMemberRequest(
    var password: String? = null,
    var nickname: String? = null,
    var profileImagePath: String? = null,
) {
    fun toServiceDto(): UpdateMemberDto =
        UpdateMemberDto(password, nickname, profileImagePath)
}