package com.whyrano.domain.post.repository

import com.whyrano.domain.post.entity.Post
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/11.
 */
interface PostRepository : JpaRepository<Post, Long> {
}