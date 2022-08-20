package com.whyrano.domain.post.service.dto

import com.whyrano.domain.member.service.dto.MemberDto
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import java.time.LocalDateTime

/**
 * Created by ShinD on 2022/08/15.
 */
data class SimplePostDto(

    val id: Long,

    val postType: PostType, // 공지 | 질문

    var title: String, // 제목

    var content: String, // 내용

    var answerCount: Int = 0, // 답변 수

    var viewCount: Int = 0, // 조회수

    var likeCount: Int = 0, // 좋아요 개수

    var commentCount: Int = 0, // 댓글 수

    var createdDate: LocalDateTime?, // 생성일

    var modifiedDate: LocalDateTime?, // 수정일

    var writerDto: MemberDto, // 작성자 정보

) {

    companion object {

        fun from(post: Post) =
            SimplePostDto(
                id = post.id !!,
                postType = post.postType,
                title = post.title,
                content = post.content,
                answerCount = post.answerCount.get(),
                viewCount = post.viewCount.get(),
                likeCount = post.likeCount.get(),
                commentCount = post.commentCount.get(),
                createdDate = post.createdDate,
                modifiedDate = post.modifiedDate,
                writerDto = MemberDto.from(post.writer !!)
            )
    }
}