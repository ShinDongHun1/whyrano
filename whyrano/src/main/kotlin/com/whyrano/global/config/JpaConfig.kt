package com.whyrano.global.config

import com.whyrano.domain.common.auditoraware.MemberAuditorAware
import com.whyrano.domain.member.repository.MemberRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

/**
 * Created by ShinD on 2022/08/09.
 */

@Configuration
@EnableJpaAuditing
class JpaConfig {

    @Bean
    fun memberAuditorAware(memberRepository: MemberRepository? = null): MemberAuditorAware {
        checkNotNull(memberRepository) { "memberRepository is null !" }
        return MemberAuditorAware(memberRepository)
    }
}