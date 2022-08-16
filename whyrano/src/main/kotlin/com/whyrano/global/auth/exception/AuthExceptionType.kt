package com.whyrano.global.auth.exception

import com.whyrano.global.exception.BaseExceptionType
import org.springframework.http.HttpStatus

/**
 * Created by ShinD on 2022/08/13.
 */
enum class AuthExceptionType(

    private val errorCode: Int,

    private val httpStatus: HttpStatus,

    private val message: String

) : BaseExceptionType{

    //== Json 로그인 관련 ==//
    NOT_ALLOWED_LOGIN_METHOD(1100, HttpStatus.METHOD_NOT_ALLOWED, "요청이 POST가 아닙니다."),
    UNSUPPORTED_LOGIN_MEDIA_TYPE(1101, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "요청이 JSON 형식이 아닙니다."),
    BAD_USERNAME_PASSWORD(1102, HttpStatus.UNAUTHORIZED, "아이디나 비밀번호가 없거나, 형식이 잘못되었습니다."),
    NOT_FOUND_MEMBER(1103, HttpStatus.UNAUTHORIZED, "유저 정보가 없습니다."),


    //== JWT 인증 관련 ==//
    EMPTY_TOKEN(1104, HttpStatus.UNAUTHORIZED, "토큰이 존재하지 않습니다."),
    BAD_TOKEN(1105, HttpStatus.UNAUTHORIZED, "잘못된 토큰입니다."), //토큰에 인증정보가 포함되지 않음.
    ALL_TOKEN_INVALID(1106, HttpStatus.UNAUTHORIZED, "AccessToken과 RefreshToken 모두 만료되었습니다. 다시 로그인을 진행해 주세요."),
    UNMATCHED_MEMBER(1107, HttpStatus.UNAUTHORIZED, "두 토큰을 가진 유저 정보가 없습니다."),


    //== 이외 예측하지 못한 오류 ==//
    ELSE(1199, HttpStatus.UNAUTHORIZED, "로그인 도중 오류가 발생하였습니다."),

    ;


    override fun errorCode() = errorCode
    override fun httpStatus() = httpStatus
    override fun message() = message
}