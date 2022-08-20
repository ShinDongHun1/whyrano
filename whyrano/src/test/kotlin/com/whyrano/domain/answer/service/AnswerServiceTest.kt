package com.whyrano.domain.answer.service

import com.ninjasquad.springmockk.MockkBean
import com.whyrano.domain.answer.entity.Answer
import com.whyrano.domain.answer.exception.AnswerException
import com.whyrano.domain.answer.exception.AnswerExceptionType
import com.whyrano.domain.answer.fixture.AnswerFixture
import com.whyrano.domain.answer.repository.AnswerRepository
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.exception.MemberException
import com.whyrano.domain.member.exception.MemberExceptionType
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.exception.PostException
import com.whyrano.domain.post.exception.PostExceptionType
import com.whyrano.domain.post.fixture.PostFixture
import com.whyrano.domain.post.repository.PostRepository
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Created by ShinD on 2022/08/20.
 */
@ExtendWith(SpringExtension::class)
@Import(AnswerService::class)
internal class AnswerServiceTest {


    @MockkBean
    private lateinit var answerRepository: AnswerRepository

    @MockkBean
    private lateinit var memberRepository: MemberRepository

    @MockkBean
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var answerService: AnswerService





    @Test
    fun `답글 작성 성공`() {

        //given
        val writer = MemberFixture.member(id = 10L)
        val post = PostFixture.post(id = 11L)
        val answer = AnswerFixture.answer(id = 12L, writer = writer, post = post)
        val cad = AnswerFixture.createAnswerDto()
        every { answerRepository.save(any()) } returns answer
        every { memberRepository.findByIdOrNull(writer.id!!) } returns writer
        every { postRepository.findByIdOrNull(post.id!!) } returns post

        //when
        val answerId = answerService.create(writer.id!!, post.id!!, cad)

        //then
        assertThat(answerId).isEqualTo(answer.id)
        verify(exactly = 1) { answerRepository.save(any()) }
    }





    @Test
    fun `답글 작성 실패 - 회원이 없는 경우`() {

        //given
        val writer = MemberFixture.member(id = 10L)
        val post = PostFixture.post(id = 11L)
        val answer = AnswerFixture.answer(id = 12L, writer = writer, post = post)
        val cad = AnswerFixture.createAnswerDto()
        every { memberRepository.findByIdOrNull(writer.id!!) } throws MemberException(MemberExceptionType.NOT_FOUND)
        every { answerRepository.save(any()) } returns answer

        //when
        val exceptionType = assertThrows<MemberException> { answerService.create(writer.id!!, post.id!!, cad) }
                            .exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(MemberExceptionType.NOT_FOUND)
        verify(exactly = 1) { memberRepository.findByIdOrNull(writer.id!!) }
        verify(exactly = 0) { answerRepository.save(any()) }
    }





    @Test
    fun `답글 작성 실패 - 질문이 없는 경우`() {
        //given
        val writer = MemberFixture.member(id = 10L)
        val post = PostFixture.post(id = 11L)
        val answer = AnswerFixture.answer(id = 12L, writer = writer, post = post)
        val cad = AnswerFixture.createAnswerDto()
        every { memberRepository.findByIdOrNull(writer.id!!) } returns writer
        every { postRepository.findByIdOrNull(post.id!!) } throws  PostException(PostExceptionType.NOT_FOUND)
        every { answerRepository.save(any()) } returns answer

        //when
        val exceptionType = assertThrows<PostException> { answerService.create(writer.id!!, post.id!!, cad) }
            .exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(PostExceptionType.NOT_FOUND)
        verify(exactly = 1) { memberRepository.findByIdOrNull(writer.id!!) }
        verify(exactly = 0) { answerRepository.save(any()) }
    }





    @Test
    fun `답글 작성 실패 - 공지에 작성하는 경우`() {
        //given
        val writer = MemberFixture.member(id = 10L)
        val post = PostFixture.post(id = 11L, postType = PostType.NOTICE)
        val answer = AnswerFixture.answer(id = 12L, writer = writer, post = post)
        val cad = AnswerFixture.createAnswerDto()
        every { memberRepository.findByIdOrNull(writer.id!!) } returns writer
        every { postRepository.findByIdOrNull(post.id!!) } returns post
        every { answerRepository.save(any()) } returns answer

        //when
        val exceptionType = assertThrows<AnswerException> { answerService.create(writer.id!!, post.id!!, cad) }
            .exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(AnswerExceptionType.CANNOT_WRITE_IN_NOTICE)
        verify(exactly = 1) { memberRepository.findByIdOrNull(writer.id!!) }
        verify(exactly = 1)  { postRepository.findByIdOrNull(post.id!!) }
        verify(exactly = 0) { answerRepository.save(any()) }
    }





    @Test
    fun `답글 작성 실패 - 블랙리스트가 작성하는 경우`() {

        //given
        val writer = MemberFixture.member(id = 10L, Role.BLACK)
        val post = PostFixture.post(id = 11L, postType = PostType.QUESTION)
        val answer = AnswerFixture.answer(id = 12L, writer = writer, post = post)
        val cad = AnswerFixture.createAnswerDto()

        every { memberRepository.findByIdOrNull(writer.id!!) } returns writer
        every { postRepository.findByIdOrNull(post.id!!) } returns post
        every { answerRepository.save(any()) } returns answer

        //when
        val exceptionType = assertThrows<AnswerException> { answerService.create(writer.id!!, post.id!!, cad) }
            .exceptionType()

        //then
        assertThat(exceptionType).isEqualTo(AnswerExceptionType.NO_AUTHORITY_WRITE_ANSWER)
        verify(exactly = 1) { memberRepository.findByIdOrNull(writer.id!!) }
        verify(exactly = 0) { answerRepository.save(any()) }
    }
}