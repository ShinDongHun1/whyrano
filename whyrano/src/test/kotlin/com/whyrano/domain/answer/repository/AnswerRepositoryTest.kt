package com.whyrano.domain.answer.repository

import com.ninjasquad.springmockk.MockkBean
import com.querydsl.jpa.impl.JPAQueryFactory
import com.whyrano.domain.answer.fixture.AnswerFixture
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.fixture.PostFixture
import com.whyrano.domain.post.repository.PostRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import javax.persistence.EntityManager
import javax.persistence.PersistenceUnitUtil

/**
 * Created by ShinD on 2022/08/21.
 */

@DataJpaTest
@MockkBean(JPAQueryFactory::class)
internal class AnswerRepositoryTest {

    @Autowired
    private lateinit var answerRepository: AnswerRepository



    @Autowired
    private lateinit var memberRepository: MemberRepository



    @Autowired
    private lateinit var postRepository: PostRepository



    @Autowired
    private lateinit var em: EntityManager

    private lateinit var unitUtil: PersistenceUnitUtil



    @BeforeEach
    fun sepUp() {
        unitUtil = em.entityManagerFactory.persistenceUnitUtil
    }



    @Test
    fun `findWithWriterByIdAndWriterId writer 페치조인 여부 테스트`() {

        //given
        val member = memberRepository.save(MemberFixture.member(id = null))
        val post = postRepository.save(PostFixture.post(id = null, writerId = member.id !!))
        val answer = answerRepository.save(AnswerFixture.answer(id = null, post = post, writer = member))
        em.flush()
        em.clear()

        //when
        val findAnswer =
            answerRepository.findWithWriterByIdAndWriterId(id = answer.id !!, writerId = member.id !!)

        //then
        assertThat(unitUtil.isLoaded(findAnswer !!.writer)).isTrue
        assertThat(unitUtil.isLoaded(findAnswer.post)).isFalse
    }



    @Test
    fun `findWithWriterAndPostById writer, post페치조인 여부 테스트`() {

        //given
        val member = memberRepository.save(MemberFixture.member(id = null))
        val post = postRepository.save(PostFixture.post(id = null, writerId = member.id !!))
        val answer = answerRepository.save(AnswerFixture.answer(id = null, post = post, writer = member))
        em.flush()
        em.clear()

        //when
        val findAnswer =
            answerRepository.findWithWriterAndPostById(id = answer.id !!)

        //then
        assertThat(unitUtil.isLoaded(findAnswer !!.writer)).isTrue
        assertThat(unitUtil.isLoaded(findAnswer.post)).isTrue
    }

}