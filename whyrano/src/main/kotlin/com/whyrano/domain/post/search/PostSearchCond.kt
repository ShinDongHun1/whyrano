package com.whyrano.domain.post.search

import com.whyrano.domain.post.entity.Type
import org.springframework.data.domain.Sort.Direction
import org.springframework.data.domain.Sort.Direction.DESC

/**
 * Created by ShinD on 2022/08/15.
 */
data class PostSearchCond(

    // 내용 일치로 검색
    val content: String? = null,
    val title: String? = null,

    val type: Type? = null,

    // 조회수가 높은거부터
    val viewCount: Direction = DESC,

    // 좋아요 수가 높은거부터
    val likeCount: Direction = DESC,

    // 답변 수가 높은거부터
    val answerCount: Direction = DESC,

    // 최신에 생성된 것 부터
    val createdDate: Direction = DESC,

    // 댓글 개수가 많은 것 부터
    val commentCount: Direction = DESC,
)
