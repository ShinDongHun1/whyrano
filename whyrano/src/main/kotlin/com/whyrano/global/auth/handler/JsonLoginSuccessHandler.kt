package com.whyrano.global.auth.handler

import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.auth.userdetails.AuthMember
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ShinD on 2022/08/09.
 */
class JsonLoginSuccessHandler(
    private val jwtService: JwtService,
) : AuthenticationSuccessHandler{

    private val log = KotlinLogging.logger {  }

    // ObjectMapper는 쓰레드에 안전하긴 하지만, 속도를 생각하면 직접 작성해두는 것이 더 낫다고 판단
    companion object {
        const val TOKEN_BODY_FORMAT = """
            {"accessToken" :"%s", "refreshToken" : "%s"}
        """
    }


    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {

        try {
            authentication.principal as AuthMember
        } catch (e: Exception){
            log.error { "authentication.principal이 AuthMember 타입이 아닙니다." }
            e.printStackTrace()
            return
        }



        // 반환 정보 설정
        setResponse(
            response = response,
            status = OK,
            contentType = APPLICATION_JSON_VALUE,
            charset = UTF_8,
            content =  tokenToJson(jwtService.createAccessAndRefreshToken(authentication.principal as AuthMember))
        )
    }


    private fun setResponse(
        response: HttpServletResponse,
        status: HttpStatus,
        contentType: String,
        charset: Charset,
        content: String,
    ) {
        response.status = status.value()
        response.contentType = contentType
        response.characterEncoding = charset.name()
        response.writer.println(content)
    }

    private fun tokenToJson(tokenDto: TokenDto): String
        = TOKEN_BODY_FORMAT.format(tokenDto.accessToken, tokenDto.refreshToken)

}