package com.whyrano.domain.post.controller.request

import com.whyrano.domain.post.service.dto.UpdatePostDto

/**
 * Created by ShinD on 2022/08/16.
 */
data class UpdatePostRequest(

    var title: String? = null,       // 게시글 제목

    var content: String? = null,    // 게시글 내용

) {

    fun toServiceDto(): UpdatePostDto {

        //공백인 경우 null 반환
        if (content?.isBlank() == true) {
            content = null
        }

        //공백인 경우 null 반환
        if (title?.isBlank() == true) {
            title = null
        }

        return UpdatePostDto(content = content, title = title)
    }
}