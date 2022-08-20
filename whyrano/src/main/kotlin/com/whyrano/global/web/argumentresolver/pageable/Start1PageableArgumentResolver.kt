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

    // application.yml 혹은 .properties 파일의 설정값을 가져옴
    private val springDataWebProperties: SpringDataWebProperties,

    ) : PageableHandlerMethodArgumentResolverSupport(), PageableArgumentResolver {


    companion object {

        private const val DEFAULT_PAGE_VALUE = 1    // 기본 페이지는 1
    }

    private val defaultPageSize =
        springDataWebProperties.pageable.defaultPageSize.toString() // 기본 페이지 사이즈는 properties 파일에 설정한 기본값 그대로 사용

    private val sortResolver: SortArgumentResolver = SortHandlerMethodArgumentResolver()

    private val log = KotlinLogging.logger { }


    override fun supportsParameter(parameter: MethodParameter): Boolean {

        // @Page 어노테이션이 달려있으며
        val hasPageAnnotation = parameter.hasParameterAnnotation(Page::class.java)

        // Pageable 타입에 할당이 가능한지
        val hasPageableType = Pageable::class.java.isAssignableFrom(parameter.parameterType)

        // @Page pageable: Pageable 인 경우에만 가능
        return hasPageAnnotation && hasPageableType
    }


    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Pageable {

        // Page 문자열로 반환
        val pageStringValue = webRequest.getParameter(getParameterNameToUse(pageParameterName, parameter))

        // 페이지를 1부터 시작하기 위해 페이지에서 1을 뺌
        val page = getPage(pageStringValue) // 페이지에서 1을 빼기, 없거나 형식이 올바르지 않은 경우 기본값(0)

        // 페이지의 크기 문자열로 반환
        val pageSizeStringValue = webRequest.getParameter(getParameterNameToUse(sizeParameterName, parameter))

        // 페이지 크기 숫자로 반환
        val pageSize = getPageSize(pageSizeStringValue) // 없거나 형식이 올바르지 않은 경우 properties 파일에 설정한 기본값 사용하도록 설정

        // 정렬 조건 가져오기
        val sort: Sort = sortResolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory)

        val pageable: Pageable = getPageable(parameter, page, pageSize)

        return if (sort.isSorted) { // 정렬 조건이 있는 경우

            PageRequest.of(pageable.pageNumber, pageable.pageSize, sort)
        } else { //정렬 조건이 없다면 기본값 사용

            // @Page 어노테이션 가져오기
            val pageAt = parameter.getParameterAnnotation(Page::class.java)

            // @Page(direction = DESC, sort = ["createdDate"]) 형식
            PageRequest.of(pageable.pageNumber, pageable.pageSize, Sort.by(pageAt !!.direction, *pageAt.sort))
        }
    }


    private fun getPageSize(pageSizeStringValue: String?): String {

        try {
            // 페이지 사이즈가 1보다 작은 경우 기본 페이지 크기로 초기화
            if (pageSizeStringValue !!.toInt() < 1) {
                return defaultPageSize
            }

            return pageSizeStringValue

        } catch (e: Exception) { // 페이지 사이즈에 문자열이 들어왔거나 하는 등의 오류 발생 시
            return defaultPageSize
        }
    }


    private fun getPage(pageStringValue: String?): String {

        return try {

            var page = pageStringValue?.toInt() ?: DEFAULT_PAGE_VALUE // 없는 경우 기본값 1

            page -- // page에서 1 빼기

            if (page < 0) {
                page = 0 // 1보다 작은 경우에도 1로 초기화
            }

            page.toString() // return
        } catch (e: Exception) {

            "0" // return
        }
    }
}