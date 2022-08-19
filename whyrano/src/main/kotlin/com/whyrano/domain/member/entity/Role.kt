package com.whyrano.domain.member.entity

/**
 * Created by ShinD on 2022/08/09.
 */
enum class Role {

    ADMIN, // 관리자

    BASIC, // 일반 유저

    BLACK, // 블랙리스트

    ;

    val authority: String = "ROLE_${this.name}"
}


/**
 * hasRole(A) : ROLE_A
 * hasAuthority(A) : A
 */