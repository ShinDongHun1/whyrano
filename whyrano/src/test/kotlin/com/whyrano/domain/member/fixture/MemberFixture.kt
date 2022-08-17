package com.whyrano.domain.member.fixture

import com.auth0.jwt.algorithms.Algorithm
import com.whyrano.domain.member.controller.request.CreateMemberRequest
import com.whyrano.domain.member.controller.request.UpdateMemberRequest
import com.whyrano.domain.member.controller.response.MemberInfoResponse
import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.service.dto.CreateMemberDto
import com.whyrano.domain.member.service.dto.MemberDto
import com.whyrano.domain.member.service.dto.UpdateMemberDto
import com.whyrano.global.auth.userdetails.AuthMember
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

/**
 * Created by ShinD on 2022/08/09.
 */
object MemberFixture {
    val AUTHORITY = Role.BASIC
    const val ID = 1L
    const val EMAIL = "default@default.com"
    const val PASSWORD = "defaultPassword123@"
    const val POINT = 10
    const val NICKNAME = "default_nickname"
    const val PROFILE_IMAGE_PATH = "https://default_profile_image_path.com"

    const val UPDATE_PASSWORD = "update_defaultPassword123@"
    const val UPDATE_NICKNAME = "update_default_nickname"
    const val UPDATE_PROFILE_IMAGE_PATH = "https://update_default_profile_image_path.com"


    val ALGORITHM =  Algorithm.HMAC512("ZG9uZ2h1bi1zaGFycC1kYnJ1YS13ZWItcHJvamVjdC11c2luZy1qd3Qtc2VjcmV0LURvbmdodW4tc3ByaW5nLWJvb3Qtand0LWJhY2stZW5kLWFuZC1qcy1jb2xsYWJv")
    val SECRRT_KEY =  "ZG9uZ2h1bi1zaGFycC1kYnJ1YS13ZWItcHJvamVjdC11c2luZy1qd3Qtc2VjcmV0LURvbmdodW4tc3ByaW5nLWJvb3Qtand0LWJhY2stZW5kLWFuZC1qcy1jb2xsYWJv"
    val ACCESS_TOKEN_EXPIRATION_PERIOED_DAY = 30L
    val REFRESH_TOKEN_EXPIRATION_PERIOED_DAY = 30L





    fun createMemberDto(
        authority: Role = AUTHORITY,
        email: String = EMAIL,
        password: String = PASSWORD,
        nickname: String = NICKNAME,
        profileImagePath: String? = PROFILE_IMAGE_PATH,
    ) =
        CreateMemberDto(authority, email, password, nickname, profileImagePath)


    fun member(
        id: Long? = null,
        authority: Role = AUTHORITY,
        email: String = EMAIL,
        password: String = PASSWORD,
        nickname: String = NICKNAME,
        point: Int = POINT,
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
        id: Long = ID,
        email: String = EMAIL,
        role: Role = Role.BASIC,
        accessTokenExpirationPeriodDay: Long = ACCESS_TOKEN_EXPIRATION_PERIOED_DAY,
    ) =
        AccessToken.create(id, email, role, accessTokenExpirationPeriodDay, ALGORITHM)

    fun refreshToken(
        refreshTokenExpirationPeriodDay: Long = REFRESH_TOKEN_EXPIRATION_PERIOED_DAY,
    ) =
        RefreshToken.create( refreshTokenExpirationPeriodDay, ALGORITHM)

    fun userDetail(
        username: String = EMAIL,
        password: String = "SECRET",
        role: Role = Role.BASIC,
    ): UserDetails =
        User.builder().username(username).password(password).roles(role.name).build()


    fun createMemberRequest(
        email: String = EMAIL,
        password: String = PASSWORD,
        nickname: String = NICKNAME,
        profileImagePath: String? = PROFILE_IMAGE_PATH
    ) =
        CreateMemberRequest(email, password, nickname, profileImagePath)


    fun updateMemberRequest(
        password: String? = UPDATE_PASSWORD,
        nickname: String? = UPDATE_NICKNAME,
        profileImagePath: String? = UPDATE_PROFILE_IMAGE_PATH,
    ) =
        UpdateMemberRequest(password, nickname, profileImagePath)

    fun authMember(
        id: Long = ID,
        email: String = EMAIL,
        password: String = "SECRET",
        role: Role = Role.BASIC
    ) =
        AuthMember(id= id, email = email, password = password, role = role)

    fun memberDto(
        id: Long = ID,
        role: Role = Role.BASIC,
        email: String = EMAIL,
        nickname: String = NICKNAME,
        point: Int = POINT,
        profileImagePath: String = PROFILE_IMAGE_PATH,
        createdDate: LocalDateTime? = LocalDateTime.now(),
        modifiedDate: LocalDateTime? = LocalDateTime.now(),
    ) =
        MemberDto(
            id = id,
            role = role,
            email = email,
            nickname = nickname,
            point = point,
            profileImagePath = profileImagePath,
            createdDate = createdDate,
            modifiedDate = modifiedDate,
        )

    fun memberInfoResponse(
        id: Long = ID,
        role: Role = Role.BASIC,
        email: String = EMAIL,
        nickname: String = NICKNAME,
        point: Int = POINT,
        profileImagePath: String = PROFILE_IMAGE_PATH,
        createdDate: LocalDateTime? = LocalDateTime.now(),
        modifiedDate: LocalDateTime? = LocalDateTime.now(),
    ) =
        MemberInfoResponse(
            id = id,
            role = role,
            email = email,
            nickname = nickname,
            point = point,
            profileImagePath = profileImagePath,
            createdDate = createdDate.toString(),
            modifiedDate = modifiedDate.toString(),
        )
}