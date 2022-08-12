package com.whyrano.domain.member.controller

import com.whyrano.domain.member.controller.dto.request.CreateMemberRequest
import com.whyrano.domain.member.controller.dto.request.UpdateMemberRequest
import com.whyrano.domain.member.service.MemberService
import com.whyrano.global.auth.userdetails.AuthMember
import com.whyrano.global.web.argumentresolver.Auth
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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
    fun signUp(@Valid @RequestBody cmr: CreateMemberRequest) : ResponseEntity<Unit> {
        val memberId = memberService.signUp(cmr.toServiceDto())

        val url = fromCurrentContextPath() // http://~~
            .path("/member/{memberId}")    // http://~~/member/{memberId}
            .buildAndExpand(memberId)      // http://~~/member/10
            .toUri()

        return ResponseEntity.created(url).build()
    }

    /**
     * 회원 수정 -> 자기 자신만 수정할 수 있음
     *
     */
    @PutMapping("/member")
    fun update(
        @Auth authMember: AuthMember,
        @RequestBody umr: UpdateMemberRequest) : ResponseEntity<Unit> {

        memberService.update(authMember.id, umr.toServiceDto())
        return ResponseEntity.ok().build()
    }





}
