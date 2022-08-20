package com.whyrano.domain.answer.repository

import com.whyrano.domain.answer.entity.Answer
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by ShinD on 2022/08/20.
 */
interface AnswerRepository : JpaRepository<Answer, Long> {

    // 답변 수정 시 사용
    @EntityGraph(attributePaths = ["writer"])
    fun findWithWriterByIdAndWriterId(id: Long, writerId: Long): Answer?



    /**
     * 답변 삭제 시 사용
     * (삭제는 어드민의 경우 자신의 것이 아니어도 삭제할수 있으므로 writerId로 조회할 수 없었음)
     */
    @EntityGraph(attributePaths = ["writer", "post"])
    fun findWithWriterAndPostById(id: Long): Answer?
}