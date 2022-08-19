package com.whyrano.domain.post.entity

/**
 * Created by ShinD on 2022/08/11.
 */

import com.whyrano.domain.common.BaseTimeEntity
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.entity.Role.BLACK
import com.whyrano.domain.post.entity.PostType.NOTICE
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.tag.entity.Tag
import com.whyrano.domain.taggedpost.entity.TaggedPost
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
    @Column(name = "post_type", nullable = false)
    val postType: PostType, // 공지 | 질문

    @Column(name = "title", nullable = false)
    var title: String, // 제목

    @Lob
    @Column(name = "content", nullable = false)
    var content: String, // 내용

    var answerCount: Int = 0, // 답변 수

    var viewCount: Int = 0, // 조회수

    var likeCount: Int = 0, // 좋아요 개수

    var commentCount: Int = 0, // 댓글 수

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "writer", nullable = false)
    var writer: Member? = null // 작성자

) : BaseTimeEntity() {





    /**
     * 작성자 설정
     */
    fun confirmWriter(writer: Member) {
        checkCreateAuthority(writer)
        this.writer = writer
    }





    /**
     * post 수정
     */
    fun update(title: String, content: String) {

        this.title = title
        this.content = content
    }





    /**
     * post 생성 권한 확인
     *
     *   공지 - 어드민만 가능
     *   질문 - 어드민, 일반 유저 가능 (블랙리스트 불가능)
     */
    fun checkCreateAuthority(writer: Member) {

        when(postType) {

            NOTICE -> if (writer.role != Role.ADMIN) throw PostException(PostExceptionType.NO_AUTHORITY_CREATE_NOTICE)

            PostType.QUESTION -> if (writer.role == BLACK) throw PostException(PostExceptionType.NO_AUTHORITY_CREATE_QUESTION)
        }
    }





    /**
     * post 수정 권한 확인
     *
     *   공지 -> 어드민이면 수정 가능
     *   질문 -> 작성자만 수정 가능
     *   단 블랙리스트인 경우 수정 불가능
     */
    fun checkUpdateAuthority(writer: Member) {

        // 블랙리스트인 경우 수정 불가능
        if (writer.isBlack()) throw PostException(PostExceptionType.NO_AUTHORITY_UPDATE_POST)

        // 블랙리스트가 아닌 경우
        when (this.postType) {

            // 공지의 경우 관리자가 아니면 수정 불가능
            NOTICE ->  if ( ! writer.isAdmin() ) throw PostException(PostExceptionType.NO_AUTHORITY_UPDATE_POST)

            // 일반 질문은 작성자만 수정 가능
            else ->  if ( ! isSameWriter(writer) ) throw PostException(PostExceptionType.NO_AUTHORITY_UPDATE_POST)
        }
    }





    /**
     * post 삭제 권한 확인
     *
     *   블랙리스트인 경우 삭제 불가능
     *   공지 -> 어드민이면 삭제 가능
     *   질문 -> 작성자 | 어드민 모두 가능
     */
    fun checkDeleteAuthority(writer: Member) {

        // 블랙리스트인 경우 삭제 불가능
        if (writer.isBlack()) throw PostException(PostExceptionType.NO_AUTHORITY_DELETE_POST)

        // 블랙리스트가 아닌 경우
        when (this.postType) {

            // 공지의 경우 관리자가 아니면 삭제 불가능
            NOTICE ->  if ( ! writer.isAdmin() ) throw PostException(PostExceptionType.NO_AUTHORITY_DELETE_POST)

            else ->
                // 작성자가 아니고, 관리자도 아닌 경우 삭제 불가능
                if ( ! isSameWriter(writer) && ! writer.isAdmin() ) throw PostException(PostExceptionType.NO_AUTHORITY_DELETE_POST)
        }
    }





    /**
     * 동일한 작성자인지 확인
     */
    private fun isSameWriter(writer: Member) =
        this.writer!!.id == writer.id





    /**
     * 포스트에 태그 달기
     */
    fun tagging(tags: List<Tag>): List<TaggedPost> {
        checkNotNull(this.id) { "post id is null" }
        return tags.map {
            checkNotNull(it.id) { "tag id is null" }
            TaggedPost(post = this, tag = it)
        }
    }

}
