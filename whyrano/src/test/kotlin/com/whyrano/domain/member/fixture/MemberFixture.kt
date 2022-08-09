package com.whyrano.domain.member.fixture

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.service.dto.CreateMemberDto

/**
 * Created by ShinD on 2022/08/09.
 */
object MemberFixture {
    val AUTHORITY = Role.BASIC
    const val EMAIL = "default@default.com"
    const val PASSWORD = "defaultPassword123@"
    const val NICKNAME = "default_nickname"
    const val PROFILE_IMAGE_PATH = "https://default_profile_image_path.com"


    fun createMemberDto(
        authority: Role = AUTHORITY,
        email: String = EMAIL,
        password: String = PASSWORD,
        nickname: String = NICKNAME,
        profileImagePath: String = PROFILE_IMAGE_PATH,
    ): CreateMemberDto =
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
    ): Member =
        Member(id, authority, email, password, nickname, point, profileImagePath, accessToken, refreshToken)
}