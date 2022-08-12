package com.whyrano.domain.member.controller

import com.whyrano.domain.member.controller.dto.request.CreateMemberRequest
import com.whyrano.domain.member.service.MemberService
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder.*
import javax.validation.Valid

/**
 * Created by ShinD on 2022/08/12.
 */
@RestController
class MemberController(
    private val memberService: MemberService
) {

    private val log = KotlinLogging.logger {  }


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






}
