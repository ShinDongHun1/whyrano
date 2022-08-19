package com.whyrano.domain.common

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass


/**
 * Created by ShinD on 2022/08/09.
 */
@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseTimeEntity {

    @CreatedDate
    var createdDate: LocalDateTime? = null

    @LastModifiedDate
    var modifiedDate: LocalDateTime? = null

}