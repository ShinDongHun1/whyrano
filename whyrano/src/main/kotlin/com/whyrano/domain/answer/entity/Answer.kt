package com.whyrano.domain.answer.entity

/**
 * Created by ShinD on 2022/08/20.
 */
import com.whyrano.domain.answer.exception.AnswerException
import com.whyrano.domain.answer.exception.AnswerExceptionType
import com.whyrano.domain.common.BaseTimeEntity
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
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

    var likeCount: Int = 0, // 좋아요 개수

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    var writer: Member? = null, // 작성자 (블랙리스트는 답변 불가능)

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: Post? = null, // 게시글 (공지에는 작성 불가능)

) : BaseTimeEntity() {

    fun checkAuthorityAndSetWriter(writer: Member) {

        // 블랙리스트라면 예외 발생
        if (writer.role == Role.BLACK) throw AnswerException(AnswerExceptionType.NO_AUTHORITY_WRITE_ANSWER)

        // 블랙리스트가 아닌 경우 작성자 세팅
        this.writer = writer
    }

    fun checkPostTypeAndSetPost(post: Post) {

        // 공지라면 예외 발생
        if (post.postType == PostType.NOTICE) throw AnswerException(AnswerExceptionType.CANNOT_WRITE_IN_NOTICE)

        // 공지가 아닌 경우 post 세팅
        this.post = post
    }





    /**
     * 수정
     */
    fun update(content: String) {

        // 수정 권한 확인
        if (! isUpdatable()) throw AnswerException(AnswerExceptionType.NO_AUTHORITY_UPDATE_ANSWER)

        // 수정
        this.content = content
    }





    /**
     * 수정 가능성 확인
     * 작성자가 블랙리스트만 아니면 됨
     */
    private fun isUpdatable() =
        writer!!.role != Role.BLACK





    /**
     * 해당 회원에 의해 삭제 가능한지 확인
     *
     * 해당 회원이 어드민이면 가능, 아니면 글쓴이 본인이거나.
     *
     * 단 본인이라도 블랙리스트인 경우에는 불가능
     */
    fun canDeletedBy(member: Member): Boolean {

        return when(member.role) {
            // 어드민인 경우 가능
            Role.ADMIN -> true

            // 블랙리스트인경우 불가능
            Role.BLACK -> false

            // 이외 경우 자기 자신이 쓴 답변인 경우 가능
            else -> writer!!.id!! == member.id
        }
    }
}