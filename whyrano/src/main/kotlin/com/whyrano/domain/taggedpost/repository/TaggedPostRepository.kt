package com.whyrano.domain.taggedpost.repository

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.taggedpost.entity.TaggedPost
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

/**
 * Created by ShinD on 2022/08/19.
 */
interface TaggedPostRepository : JpaRepository<TaggedPost, Long> {

    fun findAllByPost(post: Post): List<TaggedPost>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TaggedPost tp WHERE tp.post = :post ")
    fun deleteAllByPostInBatch(post: Post)

}