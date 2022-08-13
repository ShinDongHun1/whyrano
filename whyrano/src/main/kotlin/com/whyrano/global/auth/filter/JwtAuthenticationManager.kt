package com.whyrano.global.auth.filter

import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.global.auth.exception.AuthException
import com.whyrano.global.auth.exception.AuthExceptionType
import com.whyrano.global.auth.handler.JsonLoginSuccessHandler
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import com.whyrano.global.auth.userdetails.AuthMember
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by ShinD on 2022/08/13.
 */
class JwtAuthenticationManager(
    private val jwtService: JwtService,
) {

    private val log = KotlinLogging.logger {  }



    /**
     * AccessToken과 RefreshToken을 가지고 인증 처리
     */
    fun authenticateWithTokens(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {


        // 토큰이 하나라도 존재하지 않는 경우 예외
        val tokenDto = jwtService.extractToken(request)
        if (tokenDto?.accessToken == null || tokenDto.refreshToken == null) throw AuthException(AuthExceptionType.EMPTY_TOKEN)



        // 토큰 추출
        val accessToken = tokenDto.accessToken()
        val refreshToken = tokenDto.refreshToken()



        /**
         * AccessToken이 만료되지 않은 경우
         * (5분 이상 남았는지 검사)[extractUserDetail 에서 오류날수도 있기 때문에]
         */
        if (jwtService.isValidMoreThanMinute(accessToken = accessToken, minute = 5)) {

            // UserDetails 이 없는 경우 오류
            val authMember = jwtService.extractAuthMember(accessToken) ?: throw AuthException(AuthExceptionType.BAD_TOKEN)

            // 인증 성공
            successAuthentication(authMember)

            // 이후 필터 진행
            chain.doFilter(request, response)

            // 아래로 내려가면 안됨
            return
        }


        // AccessToken이 만료된 경우 토큰 재발급 과정을 거침
        reIssueTokens(refreshToken, accessToken, response)
    }




    /**
     * 인증 성공 처리
     */
    private fun successAuthentication(authMember: AuthMember) {
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(authMember, null, authMember.authorities)
        SecurityContextHolder.setContext(context)
    }





    /**
     * 토큰 재발급 과정
     */
    private fun reIssueTokens(
        refreshToken: RefreshToken,
        accessToken: AccessToken,
        response: HttpServletResponse,
    ) {

        // refreshToken 도 만료된 경우 예외 발생
        if (!jwtService.isValid(refreshToken)) throw AuthException(AuthExceptionType.ALL_TOKEN_INVALID)

        // 두 토큰을 가진 회원이 없는 경우 예외 발생
        val member = jwtService.findMemberByTokens(accessToken, refreshToken) ?: throw AuthException(AuthExceptionType.UNMATCHED_MEMBER)





        /**
         * AccessToken이 만료되었으나, AccessToken과 RefreshToken이 모두 유효한 경우
         */
        checkNotNull(member.id) {
            val message = "member ID is Null"
            log.error { message }
            message
        }

        // 토큰 재발급 시 들어갈 정보 생성
        val authMember = AuthMember(id = member.id!!, email = member.email, role = member.role)


        // Http 응답 설정
        setResponse(
            response = response,
            status = HttpStatus.OK,
            contentType = MediaType.APPLICATION_JSON_VALUE,
            charset = StandardCharsets.UTF_8,
            content = tokenToJson(jwtService.createAccessAndRefreshToken(authMember = authMember)) // 토큰 재발급
        )
    }


    /**
     * Http 응답 설정 메서드
     */
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



    /**
     * Jwt를 Json 문자열로 변경
     */
    private fun tokenToJson(tokenDto: TokenDto) =
        JsonLoginSuccessHandler.TOKEN_BODY_FORMAT.format(tokenDto.accessToken, tokenDto.refreshToken)

}