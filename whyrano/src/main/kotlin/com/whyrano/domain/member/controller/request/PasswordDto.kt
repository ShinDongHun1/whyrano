package com.whyrano.domain.member.controller.request

import javax.validation.constraints.NotBlank

/**
 * Created by ShinD on 2022/08/13.
 */
data class PasswordDto(

    @field:NotBlank val password: String,

    )