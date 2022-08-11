package com.whyrano.domain.common

import com.whyrano.domain.member.entity.Member
import org.springframework.data.annotation.CreatedBy
import javax.persistence.FetchType.LAZY
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MappedSuperclass

/**
 * Created by ShinD on 2022/08/09.
 */

@MappedSuperclass
abstract class BaseEntity : BaseTimeEntity() {

    @CreatedBy
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: Member? = null


    //TODO AuditorAware 추가
}