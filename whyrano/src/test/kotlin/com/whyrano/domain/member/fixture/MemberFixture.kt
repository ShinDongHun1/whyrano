package com.whyrano.domain.member.fixture

import com.auth0.jwt.algorithms.Algorithm
import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.RefreshToken
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


    val ALGORITHM =  Algorithm.HMAC512("ZG9uZ2h1bi1zaGFycC1kYnJ1YS13ZWItcHJvamVjdC11c2luZy1qd3Qtc2VjcmV0LURvbmdodW4tc3ByaW5nLWJvb3Qtand0LWJhY2stZW5kLWFuZC1qcy1jb2xsYWJv")
    val ACCESS_TOKEN_EXPIRATION_PERIOED_DAY = 30L
    val REFRESH_TOKEN_EXPIRATION_PERIOED_DAY = 30L





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
        accessToken: AccessToken? = null,
        refreshToken: RefreshToken? = null,
    ) =
        Member(id, authority, email, password, nickname, point, profileImagePath, accessToken, refreshToken)

    fun updateMemberDto(
        password: String = UPDATE_PASSWORD,
        nickname: String = UPDATE_NICKNAME,
        profileImagePath: String = UPDATE_PROFILE_IMAGE_PATH,
    )
        = UpdateMemberDto(password, nickname, profileImagePath)


    fun accessToken(
        email: String = EMAIL,
        role: Role = AUTHORITY,
        accessTokenExpirationPeriodDay: Long = ACCESS_TOKEN_EXPIRATION_PERIOED_DAY,
    ) =
        AccessToken.create(email, role.authority, accessTokenExpirationPeriodDay, ALGORITHM)

    fun refreshToken(
        refreshTokenExpirationPeriodDay: Long = REFRESH_TOKEN_EXPIRATION_PERIOED_DAY,
    ) =
        RefreshToken.create( refreshTokenExpirationPeriodDay, ALGORITHM)
}