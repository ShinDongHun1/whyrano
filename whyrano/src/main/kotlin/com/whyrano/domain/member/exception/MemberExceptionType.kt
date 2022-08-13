package com.whyrano.domain.member.exception

import com.whyrano.global.exception.BaseExceptionType
import org.springframework.http.HttpStatus

/**
 * Created by ShinD on 2022/08/13.
 */
enum class MemberExceptionType(
    private val errorCode: Int,
    private val httpStatus: HttpStatus,
    private val message: String
) : BaseExceptionType {


    ALREADY_EXIST(1000, HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    NOT_FOUND(1001, HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),
    UNMATCHED_PASSWORD(1002, HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    ;


    override fun errorCode() = errorCode
    override fun httpStatus() = httpStatus
    override fun message() = message
}