package com.whyrano.domain.post.fixture

import com.whyrano.domain.member.entity.Role
import com.whyrano.domain.member.fixture.MemberFixture
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.entity.PostType
import com.whyrano.domain.post.service.dto.CreatePostDto
import com.whyrano.domain.post.service.dto.UpdatePostDto


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
    ) =
            CreatePostDto(title = title, content = content, postType = postType)

    fun updatePostDto(
        title: String? = UPDATE_TITLE,
        content: String? = UPDATE_CONTENT,
    ) =
        UpdatePostDto(title = title, content = content)

}