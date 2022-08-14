package com.whyrano.domain.post.exception

import com.whyrano.global.exception.BaseExceptionType
import org.springframework.http.HttpStatus

/**
 * Created by ShinD on 2022/08/14.
 */
enum class PostExceptionType (
    private val errorCode: Int,
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseExceptionType {

    NO_AUTHORITY_CREATE_POST(1200, HttpStatus.FORBIDDEN, "게시물을 작성할 권한이 없습니다. (블랙리스트)"),
    NO_AUTHORITY_CREATE_NOTICE(1201, HttpStatus.FORBIDDEN, "공지를 작성할 권한이 없습니다. (관리지만 가능합니다.)"),

        ;
    override fun errorCode() = errorCode
    override fun httpStatus() = httpStatus
    override fun message() = message
}