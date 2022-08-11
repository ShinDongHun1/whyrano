package com.whyrano.global.auth.filter

import com.whyrano.global.auth.handler.JsonLoginSuccessHandler
import com.whyrano.global.auth.jwt.JwtService
import com.whyrano.global.auth.jwt.TokenDto
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by ShinD on 2022/08/11.
 */
class JwtAuthenticationFilter(
    private val uncheckedUrls: List<String> = emptyList(),
    private val jwtService: JwtService,
) : Filter {





    /**
     * AccessToken이 만료되지 않은 경우 ->
     * (5분 이상 남은 경우 -> 아래 진행, 5분 이하로 남은 경우 -> 재발급 요청 (오류날수도 있으니까))
     *  AccessToken 으로부터 email 추출 -> DB에서 조회 -> 해당 회원의 accessToken과 refeshToken이 일치하는지 확인->
     *
     *      일치하지 않은 경우 -> 잘못된 요청일 가능성이 큼 -> AccessToken, Rerefresh 토큰 둘 다 만료(DB에서 제거)시킨 후 재발급 받으라는 오류 메세지
     *      일치하는 경우 -> 인증 성공
     *
     * AccessToken이 만료된 경우 ->
     *  AccessToken과 RefeshToken으로 DB에서 회원 조회 ->
     *      있는 경우 두개가 다 일치하는지 확인 ->
     *          일치하는 경우 AceessToken 재발급, 동시에 RefreshToken도 재발급 해주기(보안을 위해 자주 변경하는 느낌?)
     */

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {

        val req = request as HttpServletRequest
        val res = response as HttpServletResponse


        // permitAll 인 경우에는 토큰을 검사하지 않음
        if(isUncheckUrl(req.requestURI))
            return chain.doFilter(request, response)

        // 토큰이 하나라도 존재하지 않는 경우 403
        val tokenDto = jwtService.extractToken(req)
        if (tokenDto?.accessToken == null || tokenDto.refreshToken == null)
            return failureAuthentication(res)





        val accessToken = tokenDto.accessToken()
        val refreshToken = tokenDto.refreshToken()

        /**
         * AccessToken이 만료되지 않은 경우
         *                               (5분 이상 남았는지 검사)
         */
        if (jwtService.isValidMoreThanMinute(accessToken = accessToken, minute = 5)){

            // UserDetails 이 없는 경우 오류
            val userDetails = jwtService.extractUserDetail(accessToken)
                ?:  return failureAuthentication(res)

            // 인증 성공
            successAuthentication(userDetails)

            // chain.doFilter()를 해주어야 해당 정보를 가지고 쭉 진행함, 만약 해당 코드가 없다면 200을 반환할 뿐 요청이 컨트롤러로 전달되지 않음
            return chain.doFilter(request, response)
        }




        /**
         * AccessToken이 만료된 경우
         */
        if (!jwtService.isValid(refreshToken)) // refreshToken 도 만료된 경우 403
            return failureAuthentication(res)

        val member = jwtService.findMemberByTokens(accessToken, refreshToken)
            ?: return failureAuthentication(res) // 두 토큰을 가진 회원이 없는 경우 403

        // 토큰 재발급
        val userDetails = User.builder()
                                            .username(member.email)
                                            .password("SECRET")
                                            .authorities(member.role.authority)
                                            .build()

        // Http 응답 설정
        setResponse(
            response = response,
            status = OK,
            contentType = APPLICATION_JSON_VALUE,
            charset = UTF_8,
            content = tokenToJson(jwtService.createAccessAndRefreshToken(userDetails = userDetails))
        )

    }



    private fun isUncheckUrl(requestURI: String) =
        uncheckedUrls.contains(requestURI)




    private fun failureAuthentication(res: HttpServletResponse) {
        setResponse(
            response = res,
            status = FORBIDDEN,
            contentType = APPLICATION_JSON_VALUE,
            charset = UTF_8,
            content = "{\"message\":\"인증에 실패하셨습니다. 토큰이 없거나 유효하지 않습니다. 로그인하여 토큰을 재발급받으세요.\"}"
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


    private fun successAuthentication(userDetails: UserDetails) {
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        context.authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        SecurityContextHolder.setContext(context)
    }


    private fun tokenToJson(tokenDto: TokenDto): String
            = JsonLoginSuccessHandler.TOKEN_BODY_FORMAT.format(tokenDto.accessToken, tokenDto.refreshToken)
}