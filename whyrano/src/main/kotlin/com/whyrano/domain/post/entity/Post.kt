package com.whyrano.domain.post.entity

/**
 * Created by ShinD on 2022/08/11.
 */

import com.whyrano.domain.common.BaseEntity
import com.whyrano.domain.common.BaseTimeEntity
import javax.persistence.*

@Entity
@Table(name = "POST")
class Post(
    @Id @Column(name = "post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity() {
}