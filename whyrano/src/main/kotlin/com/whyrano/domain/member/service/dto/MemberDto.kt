package com.whyrano.domain.member.service.dto

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import java.time.LocalDateTime

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

    var createdDate: LocalDateTime?, // 생성일

    var modifiedDate: LocalDateTime?, // 최종 정보 수정일
) {

    companion object {
        fun from(member: Member) =
            MemberDto(
                id = member.id!!,
                role = member.role,
                email = member.email,
                nickname = member.nickname,
                point = member.point,
                profileImagePath = member.profileImagePath,
                createdDate = member.createdDate,
                modifiedDate = member.modifiedDate,
            )
    }
}