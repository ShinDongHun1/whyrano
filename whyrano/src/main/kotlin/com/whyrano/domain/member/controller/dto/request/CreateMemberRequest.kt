package com.whyrano.domain.member.controller.dto.request

import com.whyrano.domain.member.service.dto.CreateMemberDto
import javax.validation.constraints.NotEmpty

data class CreateMemberRequest(
    @field:NotEmpty val email: String,
    @field:NotEmpty val password: String, //TODO : 형식 정규식으로 설정하기?
    @field:NotEmpty val nickname: String,
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
