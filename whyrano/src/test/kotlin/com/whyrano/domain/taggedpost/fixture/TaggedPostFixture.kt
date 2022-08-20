package com.whyrano.domain.taggedpost.fixture

import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.tag.entity.Tag
import com.whyrano.domain.taggedpost.entity.TaggedPost
import java.util.stream.IntStream

/**
 * Created by ShinD on 2022/08/19.
 */
object TaggedPostFixture {

    fun newTaggedPosts(
        post: Post,
        tags: List<Tag>,
    ): List<TaggedPost> =
        post.tagging(tags)

    fun savedTaggedPosts(
        post: Post,
        tags: List<Tag>,
    ): List<TaggedPost> =
        IntStream.range(0, tags.size).mapToObj { TaggedPost(id = it.toLong(), post = post, tag = tags[it]) }.toList()
}