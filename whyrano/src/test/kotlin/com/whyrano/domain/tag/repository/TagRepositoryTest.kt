package com.whyrano.domain.tag.repository

import com.ninjasquad.springmockk.MockkBean
import com.querydsl.jpa.impl.JPAQueryFactory
import com.whyrano.domain.tag.fixture.TagFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

/**
 * Created by ShinD on 2022/08/19.
 */
@DataJpaTest
@MockkBean(JPAQueryFactory::class)
class TagRepositoryTest {

    @Autowired
    private lateinit var tagRepository: TagRepository



    @Test
    fun `saveAll 동작- 가장 빠름`() {

        //given
        val newTags = TagFixture.newTags(5)

        //when
        val saveAll = tagRepository.saveAll(newTags)

        //then
        newTags.forEach { assertThat(it.isNew).isFalse() }
        saveAll.forEach { assertThat(it.isNew).isFalse() }
    }



    @Test
    fun `for + save 동작 - 가장 느림`() {

        //given
        //val newTags = TagFixture.newTags(5)

        //when
//        for (newTag in newTags) {
//            tagRepository.save(newTag)
//        }
        //then
        //newTags.forEach{ assertThat(it.isNew).isFalse() }
    }



    @Test
    fun `foreach - save 동작 - 적당히 느림`() {

        //given
        //val newTags = TagFixture.newTags(5)

        //when
        //newTags.forEach {  tagRepository.save(it) }

        //then
    }
}