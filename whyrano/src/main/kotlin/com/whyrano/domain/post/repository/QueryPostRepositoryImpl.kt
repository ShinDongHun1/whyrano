package com.whyrano.domain.post.repository

import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.whyrano.domain.member.entity.QMember
import com.whyrano.domain.member.entity.QMember.member
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.QPost.post
import com.whyrano.domain.post.search.PostSearchCond
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

/**
 * Created by ShinD on 2022/08/15.
 *
 * https://gimquokka.github.io/spring%20data/Querydsl%EC%9D%84-%ED%99%9C%EC%9A%A9%ED%95%9C-%EB%B3%B5%EC%9E%A1%ED%95%9C-%EB%8F%99%EC%A0%81%EC%BF%BC%EB%A6%AC-%EA%B5%AC%ED%98%84%EA%B8%B0/
 */
@Repository
class QueryPostRepositoryImpl(
    private val query: JPAQueryFactory,
) : QueryPostRepository {

    override fun search(cond: PostSearchCond, pageable: Pageable): Page<Post> {

        val contentQuery = query.selectFrom(post)

            .join(post.writer, member)
            .fetchJoin()

            .where(
                titleContains(),
                contentContains(),
                typeEq(),
            )
            .orderBy(
                sortByViewCount(),
                sortByLikeCount(),
                sortByAnswerCount(),
                sortByCreatedDate(),
                sortByCommentCount(),
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = query.select(post.count())
            .from(post)

        return PageableExecutionUtils.getPage(contentQuery, pageable) {countQuery.fetchOne()!!}
    }


    private fun titleContains(): BooleanExpression? {
        TODO("Not yet implemented")
    }

    private fun contentContains(): BooleanExpression? {
        TODO("Not yet implemented")
    }

    private fun typeEq(): BooleanExpression? {
        TODO("Not yet implemented")
    }

    private fun sortByViewCount(): OrderSpecifier<*>? {
        TODO("Not yet implemented")
    }

    private fun sortByLikeCount(): OrderSpecifier<*>? {
        TODO("Not yet implemented")
    }

    private fun sortByAnswerCount(): OrderSpecifier<*>? {
        TODO("Not yet implemented")
    }

    private fun sortByCreatedDate(): OrderSpecifier<*>? {
        TODO("Not yet implemented")
    }

    private fun sortByCommentCount(): OrderSpecifier<*>? {
        TODO("Not yet implemented")
    }
}
