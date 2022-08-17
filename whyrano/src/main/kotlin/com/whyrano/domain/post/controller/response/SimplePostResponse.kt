package com.whyrano.domain.post.controller.response

import com.whyrano.domain.member.controller.response.MemberInfoResponse
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.service.dto.SimplePostDto

/**
 * Created by ShinD on 2022/08/17.
 */
data class SimplePostResponse(

    val id: Long,

    val postType: PostType, // 공지 | 질문

    var title: String, // 제목

    var content: String, // 내용

    var answerCount: Int = 0, // 답변 수

    var viewCount: Int = 0, // 조회수

    var likeCount: Int = 0, // 좋아요 개수

    var commentCount: Int = 0, // 댓글 수

    var createdDate: String?, // 생성일

    var modifiedDate: String?, // 수정일

    var writerInfo: MemberInfoResponse, // 작성자 정보
) {

    companion object {

        fun from(spd: SimplePostDto) =
            SimplePostResponse(
                id = spd.id,
                postType = spd.postType,
                title = spd.title,
                content = spd.content,
                answerCount = spd.answerCount,
                viewCount = spd.viewCount,
                likeCount = spd.likeCount,
                commentCount = spd.commentCount,
                createdDate = spd.createdDate.toString(),
                modifiedDate = spd.modifiedDate.toString(),
                writerInfo = MemberInfoResponse.from(spd.writerDto)
            )
    }
}