package com.whyrano.domain.tag.repository

import com.whyrano.domain.tag.entity.Tag
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/19.
 */
interface TagRepository : JpaRepository<Tag, Long>{
}