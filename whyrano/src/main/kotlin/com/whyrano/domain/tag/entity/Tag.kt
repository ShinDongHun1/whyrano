package com.whyrano.domain.tag.entity

/**
 * Created by ShinD on 2022/08/19.
 */
import com.whyrano.domain.common.BaseTimeEntity
import javax.persistence.*

@Entity
@Table(name = "TAG")
class Tag(

    @Id @Column(name = "tag_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String    // 태그 이름 (중복 불가능)

) : BaseTimeEntity() {

    val isNew: Boolean
        get() = id == null

}