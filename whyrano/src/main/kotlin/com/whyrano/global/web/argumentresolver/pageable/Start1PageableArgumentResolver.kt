package com.whyrano.global.web.argumentresolver.pageable

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties
import org.springframework.core.MethodParameter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableArgumentResolver
import org.springframework.data.web.PageableHandlerMethodArgumentResolverSupport
import org.springframework.data.web.SortArgumentResolver
import org.springframework.data.web.SortHandlerMethodArgumentResolver
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.ModelAndViewContainer





/**
 * 1페이지부터 시작하는 PageableArgumentResolver
 *  PageableHandlerMethodArgumentResolver 참고하여 구현
 */
@Component
class Start1PageableArgumentResolver(

    private val springDataWebProperties: SpringDataWebProperties,

) : PageableHandlerMethodArgumentResolverSupport(), PageableArgumentResolver {


    companion object {
        private const val DEFAULT_PAGE_VALUE = 1    // 기본 페이지는 1
    }

    private val defaultPageSize = springDataWebProperties.pageable.defaultPageSize.toString() // 기본 페이지 사이즈는 properties 파일에 설정한 기본값 그대로 사용

    private val log = KotlinLogging.logger { }

    private val sortResolver: SortArgumentResolver = SortHandlerMethodArgumentResolver()




    override fun supportsParameter(parameter: MethodParameter): Boolean {

        val hasPageAnnotation = parameter.hasParameterAnnotation(Page::class.java)

        val hasPageableType = Pageable::class.java.isAssignableFrom(parameter.parameterType)

        return hasPageAnnotation && hasPageableType
    }




    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Pageable {

        val pageStringValue = webRequest.getParameter(getParameterNameToUse(pageParameterName, parameter))

        val page = getPage(pageStringValue) // 페이지에서 1을 빼기, 없거나 형식이 올바르지 않은 경우 기본값(0)

        val pageSizeStringValue = webRequest.getParameter(getParameterNameToUse(sizeParameterName, parameter))

        val pageSize = getPageSize(pageSizeStringValue) // 없거나 형식이 올바르지 않은 경우 properties 파일에 설정한 기본값 사용하도록 설정

        val sort: Sort = sortResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        val pageable: Pageable = getPageable(parameter, page, pageSize)

        return if (sort.isSorted) { PageRequest.of(pageable.pageNumber, pageable.pageSize, sort) }
               else { pageable }
    }



    private fun getPageSize(pageSizeStringValue: String?): String{

        try {
            if ( pageSizeStringValue!!.toInt() < 1) {
                return defaultPageSize
            }
            return pageSizeStringValue
        }
        catch (e: Exception) {
            return defaultPageSize
        }
    }




    private fun getPage(pageStringValue: String?): String {

        return try {

            var page = pageStringValue?.toInt() ?: DEFAULT_PAGE_VALUE // 없는 경우 기본값 1

            page-- // page에서 1 빼기

            if (page < 0) {
                page = 0 // 1보다 작은 경우에도 1로 초기화
            }

            page.toString() // return
        }
        catch (e: Exception) {

            "0" // return
        }
    }
}