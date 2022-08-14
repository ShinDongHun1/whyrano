package com.whyrano.domain.post.entity

/**
 * Created by ShinD on 2022/08/11.
 */

import com.whyrano.domain.common.BaseEntity
import com.whyrano.domain.common.BaseTimeEntity
import javax.persistence.*
import javax.persistence.EnumType.STRING
import javax.persistence.GenerationType.IDENTITY

@Entity
@Table(name = "POST")
class Post(
    @Id @Column(name = "post_id")
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,

    @Enumerated(STRING)
    @Column(name = "type", nullable = false)
    val type: Type, // 공지 | 질문

    @Column(name = "title", nullable = false)
    var title: String, // 제목

    @Lob
    @Column(name = "content", nullable = false)
    var content: String, // 내용

    var answerCount: Int, // 답변 수

    var viewCount: Int, // 조회수

    var likeCount: Int, // 좋아요 개수


) : BaseEntity() { // 작성자는 BaseEntity에 포함
}