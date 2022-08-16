package com.whyrano.domain.member.controller.dto.request

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

/**
 * Created by ShinD on 2022/08/13.
 */
data class PasswordDto(

    @field:NotBlank val password: String

)