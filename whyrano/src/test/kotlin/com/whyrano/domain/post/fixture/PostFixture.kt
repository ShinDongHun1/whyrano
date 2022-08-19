package com.whyrano.domain.post.fixture

import com.whyrano.domain.member.controller.response.MemberInfoResponse
import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.service.dto.MemberDto
import com.whyrano.domain.post.controller.request.CreatePostRequest
import com.whyrano.domain.post.controller.request.UpdatePostRequest
import com.whyrano.domain.post.controller.response.SimplePostResponse
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.search.PostSearchCond
import com.whyrano.domain.post.service.dto.CreatePostDto
import com.whyrano.domain.post.service.dto.SimplePostDto
import com.whyrano.domain.post.service.dto.UpdatePostDto
import com.whyrano.domain.tag.dto.TagDto
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import java.time.LocalDateTime


/**
 * Created by ShinD on 2022/08/14.
 */
object PostFixture {


    const val ID = 10L
    const val TITLE = "example title"
    const val UPDATE_TITLE = "example update title"
    const val CONTENT = "example content"
    const val UPDATE_CONTENT = "example update content"
    const val ANSWER_COUNT = 0
    const val COMMENT_COUNT = 0
    const val VIEW_COUNT = 0
    const val LIKE_COUNT = 0



    fun post(
        id: Long? = ID,
        postType: PostType = PostType.QUESTION,
        title: String = TITLE,
        content: String = CONTENT,
        answerCount: Int = ANSWER_COUNT,
        commentCount: Int = COMMENT_COUNT,
        viewCount: Int = VIEW_COUNT,
        likeCount: Int = LIKE_COUNT,
        writerId: Long = MemberFixture.ID,
        writerRole: Role = Role.BLACK,
    ) =
        Post(id = id, postType = postType, title = title, content = content, answerCount = answerCount, viewCount = viewCount, commentCount = commentCount,likeCount = likeCount, writer = member(id = writerId, authority = writerRole))


    fun createPostDto(
        postType: PostType = PostType.QUESTION,
        title: String = TITLE,
        content: String = CONTENT,
        tagDtos: List<TagDto> = emptyList(),
    ) =
            CreatePostDto(title = title, content = content, postType = postType, tags = tagDtos)

    fun updatePostDto(
        title: String = UPDATE_TITLE,
        content: String = UPDATE_CONTENT,
        tagDtos: List<TagDto> = emptyList(),
    ) =
        UpdatePostDto(title = title, content = content, tags = tagDtos)


    fun postSearchCond(
        title: String? = null,
        content: String? = null,
        postType: PostType? = null,
    ) =
        PostSearchCond(title = title, content = content, postType = postType)



    fun postPageable(
        page: Int = 0,
        size: Int = 0,
        orders: List<Order> = emptyList()
    ) =
        PageRequest.of(page, size, Sort.by(orders))

    fun createPostRequest(
        postType: PostType = PostType.QUESTION,
        title: String = TITLE,
        content: String = CONTENT,
        tagDtos: List<TagDto> = emptyList(),
    ) =
        CreatePostRequest(title = title, content = content, postType = postType, tags = tagDtos)

    fun updatePostRequest(
        title: String = TITLE,
        content: String = CONTENT,
        tagDtos: List<TagDto> = emptyList(),
    ) =
        UpdatePostRequest(
            title = title,
            content = content,
            tags = tagDtos
        )

    fun simplePostDto(
        id: Long = ID,
        postType: PostType = PostType.QUESTION, // 공지 | 질문
        title: String = TITLE, // 제목
        content: String = CONTENT, // 내용
        answerCount: Int = ANSWER_COUNT, // 답변 수
        viewCount: Int = VIEW_COUNT, // 조회수
        likeCount: Int = LIKE_COUNT, // 좋아요 개수
        commentCount: Int = COMMENT_COUNT, // 댓글 수
        createdDate: LocalDateTime? = LocalDateTime.now(), // 생성일
        modifiedDate: LocalDateTime? = LocalDateTime.now(), // 수정일
        writerDto: MemberDto = MemberFixture.memberDto(), // 작성자 정보
    ) =
        SimplePostDto(
            id = id,
            postType = postType,
            title = title,
            content = content,
            answerCount = answerCount,
            viewCount = viewCount,
            likeCount = likeCount,
            commentCount = commentCount,
            createdDate = createdDate,
            modifiedDate = modifiedDate,
            writerDto = writerDto
        )

    fun simplePostResponse(
        id: Long = ID,
        postType: PostType = PostType.QUESTION, // 공지 | 질문
        title: String = TITLE, // 제목
        content: String = CONTENT, // 내용
        answerCount: Int = ANSWER_COUNT, // 답변 수
        viewCount: Int = VIEW_COUNT, // 조회수
        likeCount: Int = LIKE_COUNT, // 좋아요 개수
        commentCount: Int = COMMENT_COUNT, // 댓글 수
        createdDate: LocalDateTime? = LocalDateTime.now(), // 생성일
        modifiedDate: LocalDateTime? = LocalDateTime.now(), // 수정일
        writerInfoResponse: MemberInfoResponse = MemberFixture.memberInfoResponse(), // 작성자 정보
    ) =
        SimplePostResponse(
            id = id,
            postType = postType,
            title = title,
            content = content,
            answerCount = answerCount,
            viewCount = viewCount,
            likeCount = likeCount,
            commentCount = commentCount,
            createdDate = createdDate.toString(),
            modifiedDate = modifiedDate.toString(),
            writerInfo = writerInfoResponse
        )
}