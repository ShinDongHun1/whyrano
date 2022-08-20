package com.whyrano.domain.answer.repository

import com.whyrano.domain.answer.entity.Answer
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/20.
 */
interface AnswerRepository : JpaRepository<Answer, Long>{
}