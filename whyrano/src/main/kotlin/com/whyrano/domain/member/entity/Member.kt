package com.whyrano.domain.member.entity

/**
 * Created by ShinD on 2022/08/09.
 */
import com.whyrano.domain.common.BaseTimeEntity
import javax.persistence.*

@Entity
@Table(
    name = "MEMBER",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email"], name = "unique_email")]
    )
class Member(

    @Id @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null, // PK

    @Column(length = 10, nullable = false)
    var authority: Role, // 권한

    @Column(name = "email", length = 50, nullable = false)
    var email: String, // 이메일

    @Column(nullable = false)
    var password: String, // 비밀번호(암호화)

    @Column(nullable = false)
    var nickname: String, // 닉네임

    var point: Int = 0, // 포인트

    @Column(nullable = true)
    var profileImagePath: String, // 프로필 사진 경로 (https://~~)

    @Column(nullable = true)
    var accessToken: String? = null, // access token 내용 (JWT)

    @Column(nullable = true)
    var refreshToken: String? = null, // refresh token 내용 (JWT)

    ) : BaseTimeEntity() {


    fun update(nickname: String?, password: String?, profileImagePath: String?) {
        nickname?.let { this.nickname = it }
        password?.let { this.password = it }
        profileImagePath?.let { this.profileImagePath = it }
    }
}