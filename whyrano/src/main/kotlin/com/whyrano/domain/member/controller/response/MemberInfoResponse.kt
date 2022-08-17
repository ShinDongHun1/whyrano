package com.whyrano.domain.member.controller.response

import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.service.dto.MemberDto

/**
 * Created by ShinD on 2022/08/17.
 */
data class MemberInfoResponse(

    val id: Long,

    var role: Role, // 권한

    var email: String, // 이메일

    var nickname: String, // 닉네임

    var point: Int = 0, // 포인트

    var profileImagePath: String?, // 프로필 사진 경로 (https://~~)

    var createdDate: String?, // 생성일

    var modifiedDate: String?, // 최종 정보 수정일

) {

    companion object {

        fun from(writerDto: MemberDto) =
            MemberInfoResponse(
                id = writerDto.id,
                role = writerDto.role,
                email = writerDto.email,
                nickname = writerDto.nickname,
                point = writerDto.point,
                profileImagePath = writerDto.profileImagePath,
                createdDate = writerDto.createdDate?.toString(),
                modifiedDate = writerDto.modifiedDate?.toString(),
            )
    }
}