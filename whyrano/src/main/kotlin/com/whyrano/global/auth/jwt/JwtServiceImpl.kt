package com.whyrano.global.auth.jwt

import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.algorithms.Algorithm.HMAC512
import com.whyrano.domain.member.entity.AccessToken
import com.whyrano.domain.member.entity.RefreshToken
import com.whyrano.domain.member.entity.Token
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_NAME
import com.whyrano.global.auth.jwt.JwtService.Companion.ACCESS_TOKEN_HEADER_PREFIX
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest


/**
 * Created by ShinD on 2022/08/09.
 */

@Service
class JwtServiceImpl(
    private val memberRepository: MemberRepository,
    private val jwtProperties: JwtProperties,
) : JwtService{

    private lateinit var algorithm: Algorithm


    @PostConstruct
    private fun setAlgorithm() {
        algorithm = HMAC512(jwtProperties.secretKey)
    }


    /**
     * 회원 인증 정보로부터 AccessToken과 RefreshToken 추출
     */
    @Transactional
    override fun createAccessAndRefreshToken(userDetails: UserDetails): TokenDto {

        // 인증 정보로부터 email 추출
        val email = userDetails.username

        // 이메일로 회원 찾아오기 -> 영속성 컨텍스트에 회원 저장
        val member = memberRepository.findByEmail(email) ?: throw TODO("MEmberException 구현!")

        // AccessToken 발급
        val accessToken = AccessToken.create(
            email = email,
            accessTokenExpirationPeriodDay = jwtProperties.accessTokenExpirationPeriodDay,
            algorithm = algorithm
        )

        // RefreshToken 발급
        val refreshToken = RefreshToken.create(
            refreshTokenExpirationPeriodDay = jwtProperties.refreshTokenExpirationPeriodDay,
            algorithm = algorithm
        )

        // 토큰 업데이트 -> 변경 감지로 동작
        member.updateToken(accessToken, refreshToken)


        return TokenDto(accessToken.accessToken, refreshToken.refreshToken)
    }

    /**
     * AccessToken으로부터 이메일 추출
     */
    override fun extractMemberEmail(accessToken: AccessToken) =
        accessToken.getEmail(algorithm)


    /**
     * Http Request 정보로부터 토큰이 있다면 추출
     */
    override fun extractToken(request: HttpServletRequest): TokenDto? {

        //ACCESS_TOKEN_HEADER_NAME(Authorization) 이 없는 경우 Null
        val accessTokenValue = request.getHeader(ACCESS_TOKEN_HEADER_NAME) ?: return null

        //ACCESS_TOKEN_HEADER_PREFIX(Bearer )로 시작하지 않는 경우 Null
        if (!accessTokenValue.startsWith(ACCESS_TOKEN_HEADER_PREFIX)) {
            return null
        }

        //accessToken 추출 (Bearer 제거)
        val accessToken = accessTokenValue.replace(ACCESS_TOKEN_HEADER_PREFIX, "").trim()

        //refreshToken 추출
        val refreshToken = request.getHeader(JwtService.REFRESH_TOKEN_HEADER_NAME) ?: return null

        return TokenDto(accessToken, refreshToken)
    }

    /**
     * AccessToken 혹은 RefreshToken이 유효한 상태인지 확인
     */
    override fun isValid(token: Token) =
        token.isValid(algorithm)
}