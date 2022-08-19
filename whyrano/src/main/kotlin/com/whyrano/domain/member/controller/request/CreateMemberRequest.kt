package com.whyrano.domain.member.controller.request

import com.whyrano.domain.member.service.dto.CreateMemberDto
import javax.validation.constraints.NotBlank

data class CreateMemberRequest(

    @field:NotBlank val email: String,

    @field:NotBlank val password: String, //TODO : 형식 정규식으로 설정하기?

    @field:NotBlank val nickname: String,

    val profileImagePath: String? = null,

    ) {

    fun toServiceDto(): CreateMemberDto {

        //profileImagePath가 null이 아닌 ""가 넘어온 경우 null로 변경하여 넘기기
        if (profileImagePath != null && profileImagePath.isBlank()) {

            return CreateMemberDto(email = email, password = password, nickname = nickname, profileImagePath = null)
        }

        return CreateMemberDto(email = email, password = password, nickname = nickname, profileImagePath = profileImagePath)
    }
}
