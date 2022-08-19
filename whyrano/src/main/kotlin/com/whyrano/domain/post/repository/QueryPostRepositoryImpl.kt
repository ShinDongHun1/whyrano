package com.whyrano.domain.post.repository

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order.ASC
import com.querydsl.core.types.Order.DESC
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.Predicate
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
import com.whyrano.domain.tag.entity.QTag.tag
import com.whyrano.domain.taggedpost.entity.QTaggedPost.taggedPost
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

        val beforeSortQuery = query.select(post)
            .from(post)

            // member fetch join
            .join(post.writer, member)
            .fetchJoin()

            // 태그가 붙은 포스트 모두 join
            .leftJoin(taggedPost)
            .on(post.id.eq(taggedPost.post.id))

            // 태그가 붙은 포스트와 태그를 조인, 이때 태그 붙은 포스트의 태그 이름이 검색 조건과 일치해야 함.
            .leftJoin(taggedPost.tag, tag)
            .distinct()

            // 검색 조건 설정
            .where(
                titleContains(cond.title),
                contentContains(cond.content),
                typeEq(cond.postType),
                tagContains(cond.tag),
            )

        // orderBy 설정
        val afterSortQuery = sortQuery(beforeSortQuery, pageable)

        val contentQuery = afterSortQuery
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val countQuery = query.select(post.count())
            .from(post)
            // 태그가 붙은 포스트 모두 join
            .leftJoin(taggedPost)
            .on(post.id.eq(taggedPost.post.id))

            // 태그가 붙은 포스트와 태그를 조인, 이때 태그 붙은 포스트의 태그 이름이 검색 조건과 일치해야 함.
            .leftJoin(taggedPost.tag, tag)
            .distinct()
            .where(
                titleContains(cond.title),
                contentContains(cond.content),
                typeEq(cond.postType),
                tagContains(cond.tag),
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

            // 해당 필드가 존재하지 않으면 무시
            if ( ! checkFieldExist(post.type, o.property)) continue

            beforeSortQuery.orderBy(
                OrderSpecifier(if (o.isAscending) ASC else DESC, pathBuilder.get(o.property) as Expression<out Comparable<*>>)
            )
        }

        return beforeSortQuery
    }





    /**
     * 필드가 해당 클래스에 존재하는지 확인
     */
    private fun checkFieldExist(type: Class<*>, property: String): Boolean {

        // 모든 필드명을 담을 리스트
        val allFieldNames = mutableListOf<String>()

        // 자신의 필드를 모두 추가
        allFieldNames.addAll(type.declaredFields.map { it.name })

        // 부모의 필드도 모두 추가
        allFieldNames.addAll(type.superclass.declaredFields.map { it.name })

        // 속성이 존재하는지 확인
        return allFieldNames.contains(property)

    }





    /**
     * 태그 검색
     * 공백, 대소문자 무시하고 처리
     */
    private fun tagContains(tag: String?): Predicate? {

        if ( !hasText(tag) ) return null

        val noWhiteStr= tag!!.replace(" ", "").replace("\t", "")


        return Expressions.stringTemplate("replace({0},' ','')", taggedPost.tag.name).containsIgnoreCase(noWhiteStr)
    }






    /**
     * 제목 검색
     * 공백, 대소문자 무시하고 처리
     */
    private fun titleContains(title: String?): BooleanExpression? {

        if ( !hasText(title) ) return null

        val noWhiteStr= title!!.replace(" ", "").replace("\t", "")

        return Expressions.stringTemplate("replace({0},' ','')", post.title).containsIgnoreCase(noWhiteStr)
    }





    /**
     *  내용 검색
     *  공백, 대소문자 무시하고 처리
     */
    private fun contentContains(content: String?): BooleanExpression? {

        if ( !hasText(content) ) return null

        val noWhiteStr= content!!.replace(" ", "").replace("\t", "")

        return Expressions.stringTemplate("replace({0},' ','')", post.content).containsIgnoreCase(noWhiteStr)
    }





    /**
     * 게시글 타입으로 검색
     */
    private fun typeEq(postType: PostType?): BooleanExpression? {

        return postType?.let { post.postType.eq(it) }
    }
}
