package com.whyrano.domain.answer.fixture

import com.whyrano.domain.answer.entity.Answer
import com.whyrano.domain.answer.service.dto.CreateAnswerDto
import com.whyrano.domain.answer.service.dto.UpdateAnswerDto
import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.fixture.PostFixture
import java.util.concurrent.atomic.AtomicInteger


/**
 * Created by ShinD on 2022/08/20.
 */
object AnswerFixture {

    private const val ID = 11L
    private const val CONTENT = "CONTENT"
    private const val LIKE_COUNT = 10
    private val WRITER = MemberFixture.member(id = 10)
    private val POST = PostFixture.post(id = 10, PostType.QUESTION)


    fun answer(
        id: Long? = ID,
        content: String = CONTENT,
        likeCount: Int = LIKE_COUNT,
        writer: Member? = WRITER,
        post: Post? = POST

        ) =
        Answer(
            id = id,
            content = content,
            likeCount = AtomicInteger(likeCount),
            writer = writer,
            post = post
        )

    fun createAnswerDto(
        content: String = CONTENT,

    ) = CreateAnswerDto(content = content)

    fun updateAnswerDto(
        content: String = CONTENT,

        ) = UpdateAnswerDto(content = content)
}