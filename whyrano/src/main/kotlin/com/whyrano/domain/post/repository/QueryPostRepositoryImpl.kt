package com.whyrano.domain.post.repository

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order.ASC
import com.querydsl.core.types.Order.DESC
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import com.whyrano.domain.member.entity.QMember.member
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.entity.QPost.post
import com.whyrano.domain.post.search.PostSearchCond
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import org.springframework.util.StringUtils.hasText

/**
 * Created by ShinD on 2022/08/15.
 */
@Repository
class QueryPostRepositoryImpl(
    private val query: JPAQueryFactory,
) : QueryPostRepository {


    /**
     * 생성일,
     * 조회 수,
     * 댓글 수,
     * 좋아요 수,
     * 답변 수,
     */
    override fun search(cond: PostSearchCond, pageable: Pageable): Page<Post> {

        val beforeSortQuery = query.selectFrom(post)

            .join(post.writer, member)
            .fetchJoin()

            .where(
                titleContains(cond.title),
                contentContains(cond.content),
                typeEq(cond.postType),
            )

        val afterSortQuery = sortQuery(beforeSortQuery, pageable)


        val contentQuery = afterSortQuery
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()



        val countQuery = query.select(post.count())
            .from(post)
            .where(
                titleContains(cond.title),
                contentContains(cond.content),
                typeEq(cond.postType),
            )

        return PageableExecutionUtils.getPage(contentQuery, pageable) {countQuery.fetchOne()!!}
    }




    /**
     * 정렬 기준 설정
     *
     * LocalDateTime
     * 작은거 == 가장 먼저 생성된 거
     * 큰거 == 가장 최근에 생상된 거
     * 오름차순 (ASC) : 작은거 -> 큰거
     * 내림차순 (DESC) : 큰거 -> 작은거
     */
    private fun sortQuery(beforeSortQuery: JPAQuery<Post>, pageable: Pageable): JPAQuery<Post> {

        for (o in pageable.sort) {
            val pathBuilder =  PathBuilder(post.type, post.metadata)

            beforeSortQuery.orderBy(
                OrderSpecifier(if (o.isAscending) ASC else DESC, pathBuilder.get(o.property) as Expression<out Comparable<*>>)
            )
        }
        return beforeSortQuery
    }




    /**
     * 제목 검색
     * 공백 무시하고 처리
     */
    private fun titleContains(title: String?): BooleanExpression? {

        if ( !hasText(title) ) return null

        val noWhiteStrUpperCase= title!!.replace(" ", "").replace("\t", "").uppercase()

        return Expressions.stringTemplate("replace({0},' ','')", post.title).upper().contains(noWhiteStrUpperCase)
    }


    /**
     *  내용 검색
     *  공백 무시하고 처리
     */
    private fun contentContains(content: String?): BooleanExpression? {

        if ( !hasText(content) ) return null

        val noWhiteStrUpperCase= content!!.replace(" ", "").replace("\t", "").uppercase()

        return Expressions.stringTemplate("replace({0},' ','')", post.content).upper().contains(noWhiteStrUpperCase)
    }


    /**
     * 게시글 타입으로 검색
     */
    private fun typeEq(postType: PostType?): BooleanExpression? {
        return postType?.let { post.postType.eq(it) }
    }
}
