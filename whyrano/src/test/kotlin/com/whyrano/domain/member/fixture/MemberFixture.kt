package com.whyrano.domain.member.fixture

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.service.dto.CreateMemberDto
import com.whyrano.domain.member.service.dto.UpdateMemberDto

/**
 * Created by ShinD on 2022/08/09.
 */
object MemberFixture {
    val AUTHORITY = Role.BASIC
    const val EMAIL = "default@default.com"
    const val PASSWORD = "defaultPassword123@"
    const val NICKNAME = "default_nickname"
    const val PROFILE_IMAGE_PATH = "https://default_profile_image_path.com"

    const val UPDATE_PASSWORD = "update_defaultPassword123@"
    const val UPDATE_NICKNAME = "update_default_nickname"
    const val UPDATE_PROFILE_IMAGE_PATH = "https://update_default_profile_image_path.com"


    fun createMemberDto(
        authority: Role = AUTHORITY,
        email: String = EMAIL,
        password: String = PASSWORD,
        nickname: String = NICKNAME,
        profileImagePath: String = PROFILE_IMAGE_PATH,
    ) =
        CreateMemberDto(AUTHORITY, EMAIL, PASSWORD, NICKNAME, PROFILE_IMAGE_PATH)


    fun member(
        id: Long? = null,
        authority: Role = AUTHORITY,
        email: String = EMAIL,
        password: String = PASSWORD,
        nickname: String = NICKNAME,
        point: Int = 0,
        profileImagePath: String = PROFILE_IMAGE_PATH,
        accessToken: String? = null,
        refreshToken: String? = null,
    ) =
        Member(id, authority, email, password, nickname, point, profileImagePath, accessToken, refreshToken)

    fun updateMemberDto(
        password: String = UPDATE_PASSWORD,
        nickname: String = UPDATE_NICKNAME,
        profileImagePath: String = UPDATE_PROFILE_IMAGE_PATH,
    )
        = UpdateMemberDto(password, nickname, profileImagePath)
}