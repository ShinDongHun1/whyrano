package com.whyrano.domain.common.auditoraware

import com.whyrano.domain.member.entity.Member
import com.whyrano.domain.member.fixture.MemberFixture.member
import com.whyrano.domain.member.repository.MemberRepository
import com.whyrano.domain.post.entity.Post
import com.whyrano.domain.post.repository.PostRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

/**
 * Created by ShinD on 2022/08/11.
 */
@SpringBootTest
@Transactional
internal class MemberAuditorAwareTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository
    @Autowired
    private lateinit var postRepository: PostRepository
    @Autowired
    private lateinit var em: EntityManager





    @Test
    fun `엔티티 저장 시 회원 찾아와서 저장`() {
        //given
        val member = memberRepository.save(member())
        em.flush()
        em.clear()
        setSecurityContext(member)


        //when
        val post = postRepository.save(Post())
        em.flush()
        em.clear()


        //then
        val find = postRepository.findById(post.id!!).get()
        Assertions.assertThat(find.createdBy).isNotNull
        Assertions.assertThat(find.createdBy!!.email).isEqualTo(member.email)
        Assertions.assertThat(find.createdBy!!.id).isEqualTo(member.id)

    }

    @Test
    fun `엔티티 저장 시 회원 정보가 없는 경우 예외 발생`() {
        //given
        memberRepository.save(member())
        em.flush()
        em.clear()


        //then
        assertThrows(DataIntegrityViolationException::class.java) { postRepository.save(Post()) }
    }

    @Test
    fun `엔티티 저장 시 회원이 삭제된 경우 예외 발생`() {
        //given
        setSecurityContext(member())


        //then
        assertThrows(DataIntegrityViolationException::class.java) { postRepository.save(Post()) }
    }

    private fun setSecurityContext(member: Member) {
        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        val userDetails =
            User.builder().username(member.email).password("SECRET").authorities(member.role.authority).build()
        context.authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        SecurityContextHolder.setContext(context)
    }

}
