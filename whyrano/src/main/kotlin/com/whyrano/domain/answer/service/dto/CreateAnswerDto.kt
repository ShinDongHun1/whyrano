package com.whyrano.domain.answer.service.dto

import com.whyrano.domain.answer.entity.Answer

/**
 * Created by ShinD on 2022/08/20.
 */
data class CreateAnswerDto(

    val content: String, // 내용

) {

    fun toEntity(): Answer {
        return Answer(content = content)
    }
}