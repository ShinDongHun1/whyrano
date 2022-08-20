package com.whyrano.domain.post.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by ShinD on 2022/08/21.
 */
internal class PostTest {


    @Test
    fun `멀티스레드 환경에서 답변수 증가 로직 확인`() {

        //given
        val count = 1000
        val post = Post(postType = PostType.NOTICE, title = "title", content = "content")
        val executors = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(count)

        //when
        for (i in 0 until count) {
            executors.execute {
                post.plusAnswerCount()
                latch.countDown()
            }
        }

        latch.await()

        //then
        assertThat(post.answerCount.get()).isEqualTo(count)
    }



    @Test
    fun `멀티스레드 환경에서 답변수 감소 로직 확인`() {

        //given
        val count = 1000
        val post =
            Post(postType = PostType.NOTICE, title = "title", content = "content", answerCount = AtomicInteger(count))
        val executors = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(count)

        //when
        for (i in 0 until count) {
            executors.execute {
                post.minusAnswerCount()
                latch.countDown()
            }
        }

        latch.await()

        //then
        assertThat(post.answerCount.get()).isEqualTo(0)
    }
}