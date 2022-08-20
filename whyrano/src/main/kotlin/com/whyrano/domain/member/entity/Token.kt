package com.whyrano.domain.member.entity

import com.auth0.jwt.algorithms.Algorithm

/**
 * Created by ShinD on 2022/08/10.
 */
interface Token {

    fun isValid(algorithm: Algorithm): Boolean
}