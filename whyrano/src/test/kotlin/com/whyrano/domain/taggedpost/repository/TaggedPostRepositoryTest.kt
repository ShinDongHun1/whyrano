package com.whyrano.domain.tag.repository

import com.ninjasquad.springmockk.MockkBean
import com.querydsl.jpa.impl.JPAQueryFactory
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.fixture.PostFixture
import com.whyrano.domain.post.repository.PostRepository
import com.whyrano.domain.tag.fixture.TagFixture
import com.whyrano.domain.taggedpost.fixture.TaggedPostFixture
import com.whyrano.domain.taggedpost.repository.TaggedPostRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

/**
 * Created by ShinD on 2022/08/19.
 */
@DataJpaTest
@MockkBean(JPAQueryFactory::class)
class TaggedPostRepositoryTest {

    @Autowired private lateinit var taggedPostRepository: TaggedPostRepository
    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var tagRepository: TagRepository
    @Autowired private lateinit var memberRepository: MemberRepository

    @Test
    fun `saveAll 동작- 가장 빠름`() {

        //given
        val member = MemberFixture.member(id = null)
        memberRepository.save(member)

        val post = PostFixture.post(id= null)
        post.confirmWriter(member)

        val tags = TagFixture.newTags(size = 3)

        postRepository.save(post)
        tagRepository.saveAll(tags)
        val taggedPosts = TaggedPostFixture.newTaggedPosts(post = post, tags = tags)


        //when
        val saveAll = taggedPostRepository.saveAll(taggedPosts)

        //then
        taggedPosts.forEach{ assertThat(it.id).isNotNull() }
        saveAll.forEach{ assertThat(it.id).isNotNull() }


    }


}