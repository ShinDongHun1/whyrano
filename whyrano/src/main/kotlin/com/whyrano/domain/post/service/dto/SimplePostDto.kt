package com.whyrano.domain.post.service.dto

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import java.time.LocalDateTime

/**
 * Created by ShinD on 2022/08/15.
 */
data class SimplePostDto(
    val id: Long? = null,

    val postType: PostType, // 공지 | 질문

    var title: String, // 제목

    var content: String, // 내용

    var answerCount: Int = 0, // 답변 수

    var viewCount: Int = 0, // 조회수

    var likeCount: Int = 0, // 좋아요 개수

    var commentCount: Int = 0, // 댓글 수

    var createdDate: LocalDateTime, // 생성일

    var modifiedDate: LocalDateTime, // 수정일
) {
    companion object {
        fun from(it: Post): SimplePostDto {
            TODO("Not yet implemented")
        }
    }
}