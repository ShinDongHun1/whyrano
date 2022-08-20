package com.whyrano.domain.post.repository

import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType.NOTICE
import com.whyrano.domain.post.entity.PostType.QUESTION
import com.whyrano.domain.post.fixture.PostFixture.post
import com.whyrano.domain.post.search.PostSearchCond
import com.whyrano.domain.tag.entity.Tag
import com.whyrano.domain.tag.repository.TagRepository
import com.whyrano.domain.taggedpost.repository.TaggedPostRepository
import com.whyrano.global.config.JpaConfig
import com.whyrano.global.config.QuerydslConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.domain.Sort.Order
import java.util.*

/**
 * Created by ShinD on 2022/08/16.
 */
@Import(JpaConfig::class, QuerydslConfig::class)
@DataJpaTest
internal class QueryPostRepositoryImplTest {

    @Autowired private lateinit var memberRepository: MemberRepository
    @Autowired private lateinit var postRepository: PostRepository
    @Autowired private lateinit var tagRepository: TagRepository
    @Autowired private lateinit var taggedPostRepository: TaggedPostRepository

    private var member =   member(id = null, email = "email@@", password = "pass", authority = Role.ADMIN)

    /**
     * 제목으로 검색
     * 내용으로 검색
     * 타입으로 검색
     *
     * 조회수 정렬
     * 추천수 정렬
     * 대답수 정렬
     * 생성일 정렬
     * 댓글수 정렬
     *
     */

    @BeforeEach
    fun setUp() {
        member = memberRepository.save(member)
    }



    @Test
    fun `제목으로 검색 - 공백 없이 포함된 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "title${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "title"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.title }.forEach{assertThat(it).contains("title")}
    }

    @Test
    fun `제목으로 검색 - 아무것도 포함되지 않은 경우 `() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "title${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "ti11tle"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(0)
        assertThat(search.totalPages).isEqualTo(0)
        assertThat(search.number).isEqualTo(0)
        assertThat(search.numberOfElements).isEqualTo(0)

    }

    @Test
    fun `제목으로 검색 - 검색 조건에 공백이 있으며 실제 문자열에는 공백이 없는 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "title${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "   ti           tl e        "), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.title }.forEach{assertThat(it).contains("title")}
    }



    @Test
    fun `제목으로 검색 - 검색 조건에 공백이 없으며 실제 문자열에는 공백이 있는 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "       t i t   l  e${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "title"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.title }.forEach{assertThat(it).contains("       t i t   l  e")}
    }

    @Test
    fun `제목으로 검색 - 검색조건과 실제 문자열에 공백이 있는 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "       t i t     l  e${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "t    i               t   l      e"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.title }.forEach{assertThat(it).contains("       t i t     l  e")}
    }


    @Test
    fun `제목으로 검색 - 대소문자 구분 X`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "t i tLE${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "TIt  LE"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.title }.forEach{assertThat(it).contains("t i tLE")}
    }

    @Test
    fun `제목으로 검색 - 한글의 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "가나따fl마 보 슈 썅${it}",
                content = "",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(title = "가    나따fL마   보슈썅"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)
        search.content.map { it.title }.forEach{assertThat(it).contains("가나따fl마 보 슈 썅")}
    }


    @Test
    fun `내용으로 검색 - 공백 없이 포함된 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "content${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(content = "content"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.content }.forEach{assertThat(it).contains("content")}
    }




    @Test
    fun `내용으로 검색 - 아무것도 포함되지 않은 경우 `() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "content${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(content = "cont1111ent"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(0)
        assertThat(search.totalPages).isEqualTo(0)
        assertThat(search.number).isEqualTo(0)
        assertThat(search.numberOfElements).isEqualTo(0)

    }

    @Test
    fun `내용으로 검색 - 검색 조건에 공백이 있으며 실제 문자열에는 공백이 없는 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "content${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(content = "   c on   t   e   nt     "), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.content }.forEach{assertThat(it).contains("content")}
    }



    @Test
    fun `내용으로 검색 - 검색 조건에 공백이 없으며 실제 문자열에는 공백이 있는 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "c    on ten t       ${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(content = "content"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(pageSize)

        search.content.map { it.content }.forEach{assertThat(it).contains("c    on ten t       ")}
    }


    @Test
    fun `내용으로 검색 - 검색조건과 실제 문자열에 공백이 있는 경우`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "c    on    te    n t        ${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(content  = "c o n te    n t    "), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.content }.forEach{assertThat(it).contains("c    on    te    n t        ")}
    }

    @Test
    fun `내용으로 검색 - 대소문자 구분 X`() {

        //given
        val totalCount = 10
        val pageCount = 0
        val pageSize = 10

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "c    ON    te    n t        ${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val pageable = PageRequest.of(pageCount, pageSize)

        //when
        val search = postRepository.search(PostSearchCond(content = "Con   tE    n t        "), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.content }.forEach{assertThat(it).contains("c    ON    te    n t        ")}
    }




    @Test
    fun `태그로 검색`() {

        //given
        val totalCount = 5
        val pageCount = 0
        val pageSize = 5

        val posts = mutableListOf<Post>()
        repeat(totalCount) {
            val post = post(
                id = null,
                title = "",
                content = "content${it}",
                likeCount = it, viewCount = it,
                answerCount = it, commentCount = it
            )
            post.confirmWriter(member)
            posts.add(post)
        }
        postRepository.saveAllAndFlush(posts)

        val newTag1 = Tag(name = "TAG")
        val newTag2 = Tag(name = "NAME IS XXX")
        val newTag3 = Tag(name = "AAAA AAAA")
        tagRepository.saveAllAndFlush(listOf(newTag1, newTag2, newTag3))

        val tagging = posts[0].tagging(listOf(newTag1, newTag2))
        val tagging1 = posts[1].tagging(listOf(newTag3))
        val tagging2 = posts[2].tagging(listOf(newTag2))


        taggedPostRepository.saveAllAndFlush(tagging)
        taggedPostRepository.saveAllAndFlush(tagging1)
        taggedPostRepository.saveAllAndFlush(tagging2)

        val pageable = PageRequest.of(pageCount, pageSize)


        //when
        val search = postRepository.search(PostSearchCond(tag = "n A meIs   ", content = "co"), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(2) // 태그된 포스트는 2개
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(2)

        taggedPostRepository.findAllByPost(search.content[0]).map { it.tag.name }.contains(newTag2.name)
    }






    @Test
    fun `타입으로 검색 - 공지만 검색`() {
        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 10

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
            )
            post.confirmWriter(member)
            postRepository.save(post)

            val post2 = post(
                id = null,
                postType = QUESTION,
                title = "",
                content = "",
            )
            post2.confirmWriter(member)
            postRepository.save(post2)
        }


        val pageable = PageRequest.of(pageCount,  pageSize)


        //when
        val search = postRepository.search(PostSearchCond(postType  = NOTICE), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(2)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.postType }.forEach{assertThat(it).isEqualTo(NOTICE)}
    }

    @Test
    fun `타입으로 검색 - 질문만 검색`() {
        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 10

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
            )
            post.confirmWriter(member)
            postRepository.save(post)

            val post2 = post(
                id = null,
                postType = QUESTION,
                title = "",
                content = "",
            )
            post2.confirmWriter(member)
            postRepository.save(post2)
        }


        val pageable = PageRequest.of(pageCount,  pageSize)


        //when
        val search = postRepository.search(PostSearchCond(postType  = QUESTION), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(2)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)

        search.content.map { it.postType }.forEach{assertThat(it).isEqualTo(QUESTION)}
    }

    @Test
    fun `타입으로 검색 - 설정하지 않으면 모두 검색`() {
        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 10

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
            )
            post.confirmWriter(member)
            postRepository.save(post)

            val post2 = post(
                id = null,
                postType = QUESTION,
                title = "",
                content = "",
            )
            post2.confirmWriter(member)
            postRepository.save(post2)
        }



        val pageable = PageRequest.of(pageCount,  pageSize)


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong() * 2)
        assertThat(search.totalPages).isEqualTo(3)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(10)
    }


    /**
     * LocalDateTime
     * 작은거 == 가장 먼저 생성된 거
     * 큰거 == 가장 최근에 생상된 거
     * 오름차순 (ASC) : 작은거 -> 큰거
     * 내림차순 (DESC) : 큰거 -> 작은거
     */
    @Test
    fun `생성일 오름차순 정렬`() {

        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 15

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
            )
            post.confirmWriter(member)
            postRepository.save(post)
        }


        val pageable = PageRequest.of(pageCount,  pageSize, Sort.by(ASC, "createdDate"))


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(15)

        for (i in 1 until  totalCount) {
            assertThat(search.content[i].createdDate).isAfter(search.content[i-1].createdDate)
        }
    }




    @Test
    fun `생성일 내림차순 정렬`() {

        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 15

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
                viewCount = it
            )
            post.confirmWriter(member)
            postRepository.save(post)
        }
        val pageable = PageRequest.of(pageCount,  pageSize, Sort.by(DESC, "createdDate"))


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(15)

        for (i in 1 until  totalCount) {
            assertThat(search.content[i].createdDate).isBefore(search.content[i-1].createdDate)
            //0 이 제일 큼 (제일 먼저 생성됨)
            //totalCount 가 제일 작음 (제일 나중에 생성됨 )
        }
    }



    @Test
    fun `조회수 오름차순, 댓글수 내림차순 정렬`() {

        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 15

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
                viewCount = Random().nextInt(4),
                commentCount =  Random().nextInt(4),
            )
            post.confirmWriter(member)
            postRepository.save(post)
        }
        val pageable = PageRequest.of(pageCount,  pageSize, Sort.by(Order(ASC, "viewCount"), Order(DESC, "commentCount")))


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(15)

        for (i in 1 until  totalCount) {
            //조회수 오름차순, 즉 0이 제일 작음
            assertThat(search.content[i-1].viewCount).isLessThanOrEqualTo(search.content[i].viewCount)

            if (search.content[i-1].viewCount == search.content[i].viewCount) {
                //댓글수 내림차순, 즉 0이 제일 큼
                assertThat(search.content[i-1].commentCount).isGreaterThanOrEqualTo(search.content[i].commentCount)
            }
        }
    }

    @Test
    fun `댓글수 내림차순, 조회수 오름차순 정렬`() {

        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 15

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
                viewCount = Random().nextInt(4),
                commentCount =  Random().nextInt(4),
            )
            post.confirmWriter(member)
            postRepository.save(post)
        }
        val pageable = PageRequest.of(pageCount,  pageSize, Sort.by( Order(DESC, "commentCount"), Order(ASC, "viewCount")))


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(15)

        for (i in 1 until  totalCount) {
            //댓글수 내림차순, 즉 0이 제일 큼
            assertThat(search.content[i-1].commentCount).isGreaterThanOrEqualTo(search.content[i].commentCount)


            if (search.content[i-1].commentCount == search.content[i].commentCount) {
                //조회수 오름차순, 즉 0이 제일 작음
                assertThat(search.content[i-1].viewCount).isLessThanOrEqualTo(search.content[i].viewCount)
            }
        }
    }





    @Test
    fun `아무 정렬 조건이 없다면 생성일 오름차순 정렬`() {

        //given
        val totalCount = 5
        val pageCount = 0
        val pageSize = 5

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
            )
            post.confirmWriter(member)
            postRepository.save(post)
        }


        val pageable = PageRequest.of(pageCount,  pageSize)


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(5)

        for (i in 1 until  totalCount) {
            assertThat(search.content[i].createdDate).isAfter(search.content[i-1].createdDate)
        }
    }




    @Test
    fun `없는 필드를 정렬하는 경우 무시하고 있는 조건만 사용`() {

        //given
        val totalCount = 15
        val pageCount = 0
        val pageSize = 15

        repeat(totalCount) {
            val post = post(
                id = null,
                postType = NOTICE,
                title = "",
                content = "",
                commentCount = it,
            )
            post.confirmWriter(member)
            postRepository.save(post)
        }


        val pageable = PageRequest.of(pageCount,  pageSize, Sort.by(Order(ASC, "commentCount"), Order(ASC, "createdDatㅇㅂㅈㅇㅈㅂe")))


        //when
        val search = postRepository.search(PostSearchCond(), pageable)


        //then
        assertThat(search.totalElements).isEqualTo(totalCount.toLong())
        assertThat(search.totalPages).isEqualTo(1)
        assertThat(search.number).isEqualTo(pageCount)
        assertThat(search.numberOfElements).isEqualTo(15)

        for (i in 1 until  totalCount) {
            //댓글수 내림차순, 즉 0이 제일 큼
            assertThat(search.content[i].createdDate).isAfter(search.content[i-1].createdDate)
        }
    }
}