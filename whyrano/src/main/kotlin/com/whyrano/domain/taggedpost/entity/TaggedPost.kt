package com.whyrano.domain.taggedpost.entity

/**
 * Created by ShinD on 2022/08/19.
 */
import com.whyrano.domain.common.BaseTimeEntity
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.tag.entity.Tag
import javax.persistence.*

@Entity
@Table(name = "TAGGED_POST")
class TaggedPost(

    @Id @Column(name = "tagged_post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    val post: Post, // 포스트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    val tag: Tag, // 태그

) : BaseTimeEntity() {
}