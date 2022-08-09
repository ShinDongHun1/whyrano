package com.whyrano.domain.member

/**
 * Created by ShinD on 2022/08/09.
 */

import com.whyrano.domain.common.BaseEntity
import javax.persistence.*

@Entity
@Table(name = "MEMBER")
class Member(

    @Id @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(length = 10, nullable = false)
    var authority: Role,

    @Column(length = 50, unique = true, nullable = false)
    var email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var nickname: String,

    var point: Int = 0,

    @Column(nullable = true)
    var profileImagePath: String,

    @Column(nullable = true)
    var accessToken: String,

    @Column(nullable = true)
    var refreshToken: String,

    ) : BaseEntity() {
}