package com.whyrano.domain.post.entity

/**
 * Created by ShinD on 2022/08/11.
 */

import com.whyrano.domain.common.BaseTimeEntity
import com.whyrano.domain.member.entity.Member
import org.springframework.data.annotation.CreatedBy
import javax.persistence.*
import javax.persistence.EnumType.STRING
import javax.persistence.FetchType.LAZY
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

    var answerCount: Int = 0, // 답변 수

    var viewCount: Int = 0, // 조회수

    var likeCount: Int = 0, // 좋아요 개수



    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "writer", nullable = false)
    var writer: Member? = null // 작성자

) : BaseTimeEntity() {


    /**
     * 작성자 설정
     */
    fun confirmWriter(writer: Member) {
        this.writer = writer
    }
}