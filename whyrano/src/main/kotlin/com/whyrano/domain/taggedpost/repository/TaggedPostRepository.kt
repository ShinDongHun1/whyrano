package com.whyrano.domain.taggedpost.repository

import com.whyrano.domain.taggedpost.entity.TaggedPost
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/19.
 */
interface TaggedPostRepository : JpaRepository<TaggedPost, Long> {
}