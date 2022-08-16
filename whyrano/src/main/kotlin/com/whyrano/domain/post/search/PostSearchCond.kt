package com.whyrano.domain.post.search

import com.whyrano.domain.post.entity.PostType

/**
 * Created by ShinD on 2022/08/15.
 */
data class PostSearchCond(

    val content: String? = null,        // 검색할 내용

    val title: String? = null,          // 검색할 제목

    val postType: PostType? = null,     // 검색할 타입

)
