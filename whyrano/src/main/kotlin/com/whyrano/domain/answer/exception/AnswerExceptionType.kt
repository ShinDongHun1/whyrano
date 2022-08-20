package com.whyrano.domain.answer.exception

import com.whyrano.global.exception.BaseExceptionType
import org.springframework.http.HttpStatus

/**
 * Created by ShinD on 2022/08/20.
 */
enum class AnswerExceptionType(

    private val errorCode: Int,

    private val httpStatus: HttpStatus,

    private val message: String

) : BaseExceptionType {

    CANNOT_WRITE_IN_NOTICE(1300, HttpStatus.BAD_REQUEST, "공지에는 답변을 작성하실 수 없습니다.\n 댓글만 작성 가능합니다."),
    NO_AUTHORITY_WRITE_ANSWER(1301, HttpStatus.FORBIDDEN, "답변을 작성할 권한이 없습니다. (블랙리스트)"),
    NOT_FOUND(1302, HttpStatus.NOT_FOUND, "답글이 없습니다."),
    NO_AUTHORITY_UPDATE_ANSWER(1303, HttpStatus.FORBIDDEN, "답변을 수정할 권한이 없습니다. (블랙리스트거나 본인이 아님)"),
    NO_AUTHORITY_DELETE_ANSWER(1304, HttpStatus.FORBIDDEN, "답변을 삭제할 권한이 없습니다. (블랙리스트거나 본인이 아님)"),


    ;

    override fun errorCode() = errorCode

    override fun httpStatus() = httpStatus

    override fun message() = message
}