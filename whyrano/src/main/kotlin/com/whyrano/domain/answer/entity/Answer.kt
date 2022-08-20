package com.whyrano.domain.answer.entity

/**
 * Created by ShinD on 2022/08/20.
 */
import com.whyrano.domain.common.BaseTimeEntity
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.post.entity.Post
import javax.persistence.*
import javax.persistence.FetchType.*
import javax.persistence.GenerationType.*

@Entity
@Table(name = "ANSWER")
class Answer(

    @Id @Column(name = "answer_id")
    @GeneratedValue(strategy = IDENTITY)
    val id: Long? = null,

    var content: String, // 내용

    var likeCount: Int, // 좋아요 개수

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    var writer: Member? = null, // 작성자 (블랙리스트는 답변 불가능)

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: Post? = null, // 게시글 (공지에는 작성 불가능)

) : BaseTimeEntity() {
}