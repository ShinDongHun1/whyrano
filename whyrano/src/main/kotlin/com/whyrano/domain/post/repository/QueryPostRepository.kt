package com.whyrano.domain.post.repository

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.search.PostSearchCond
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Created by ShinD on 2022/08/15.
 */
interface QueryPostRepository {

    fun search(cond: PostSearchCond, pageable: Pageable): Page<Post>
}