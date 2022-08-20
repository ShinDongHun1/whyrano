package com.whyrano.global.web.argumentresolver.pageable

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.core.MethodParameter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

/**
 * Created by ShinD on 2022/08/16.
 */
internal class Start1PageableArgumentResolverTest {

    private lateinit var start1PageableArgumentResolver: Start1PageableArgumentResolver



    @BeforeEach
    fun setUp() {
        start1PageableArgumentResolver = Start1PageableArgumentResolver(SpringDataWebProperties())
    }



    @Test
    fun `@Page 붙은 Pageable 파라미터를 지원`() {
        val method = Start1PageableArgumentResolverTest::class.java.methods.first { it.name.equals("testMethod") }
        val parameter = method.parameters[0]
        val pageable = PageRequest.of(0, 1)

        assertThat(start1PageableArgumentResolver.supportsParameter(MethodParameter.forParameter(parameter))).isTrue
    }



    @Test
    fun `@Page 붙은 다른 타입의 파라미터를 지원하지 않음`() {
        val method = Start1PageableArgumentResolverTest::class.java.methods.first { it.name.equals("testMethod2") }
        val parameter = method.parameters[0]
        val pageable = PageRequest.of(0, 1)

        assertThat(start1PageableArgumentResolver.supportsParameter(MethodParameter.forParameter(parameter))).isFalse
    }



    @Test
    fun `@Page 안붙은 Pageable 파라미터를 지원하지 않음`() {
        val method = Start1PageableArgumentResolverTest::class.java.methods.first { it.name.equals("testMethod3") }
        val parameter = method.parameters[0]
        val pageable = PageRequest.of(0, 1)

        assertThat(start1PageableArgumentResolver.supportsParameter(MethodParameter.forParameter(parameter))).isFalse
    }


    //  변환 테스트하는법 모르겠음..ㅠㅠ

    fun testMethod(@Page pageable: Pageable) {
    }

    fun testMethod2(@Page any: Any) {
    }

    fun testMethod3(pageable: Pageable) {
    }
}