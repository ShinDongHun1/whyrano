package com.whyrano.domain.member.controller

import com.whyrano.domain.member.controller.dto.request.CreateMemberRequest
import com.whyrano.domain.member.controller.dto.request.PasswordDto
import com.whyrano.domain.member.controller.dto.request.UpdateMemberRequest
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.userdetails.AuthMember
import com.whyrano.global.web.argumentresolver.auth.Auth
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath
import javax.validation.Valid

/**
 * Created by ShinD on 2022/08/12.
 */
@RestController
class MemberController(
    private val memberService: MemberService
) {

    /**
     * 회원 가입 요청
     */
    @PostMapping("/signup")
    fun signUp(
        @Valid @RequestBody cmr: CreateMemberRequest
    ) : ResponseEntity<Unit> {

        val memberId = memberService.signUp(cmr.toServiceDto())

        val url = fromCurrentContextPath() // http://~~
            .path("/member/{memberId}")    // http://~~/member/{memberId}
            .buildAndExpand(memberId)      // http://~~/member/10
            .toUri()

        return ResponseEntity.created(url).build()
    }





    /**
     * 회원 수정
     */
    @PutMapping("/member")
    fun update(
        @Auth authMember: AuthMember,
        @RequestBody umr: UpdateMemberRequest
    ): ResponseEntity<Unit> {

        memberService.update(authMember.id, umr.toServiceDto())

        return ResponseEntity.ok().build()
    }





    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/member")
    fun delete(
        @Auth authMember: AuthMember,
        @Valid @RequestBody passwordDto: PasswordDto
    ): ResponseEntity<Unit> {

        memberService.delete(authMember.id, passwordDto.password)

        return ResponseEntity.noContent().build()
    }
}
