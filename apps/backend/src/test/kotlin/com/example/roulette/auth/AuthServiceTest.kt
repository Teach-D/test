package com.example.roulette.auth

import com.example.roulette.auth.dto.LoginRequest
import com.example.roulette.auth.entity.Member
import com.example.roulette.auth.entity.MemberRole
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class AuthServiceTest {
    private lateinit var memberRepository: MemberRepository
    private lateinit var jwtProvider: JwtProvider
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        memberRepository = mockk()
        jwtProvider = mockk()
        authService = AuthService(memberRepository, jwtProvider)
    }

    @Test
    fun `기존 회원으로 로그인하면 JWT를 발급한다`() {
        // given
        val member = Member(nickname = "tester", role = MemberRole.USER, id = 1L)
        every { memberRepository.findByNickname("tester") } returns Optional.of(member)
        every { jwtProvider.createToken(1L, "USER") } returns "test-token"

        // when
        val result = authService.login(LoginRequest(nickname = "tester"))

        // then
        assertEquals("test-token", result.accessToken)
        assertEquals(1L, result.memberId)
        assertEquals("tester", result.nickname)
        assertEquals("USER", result.role)
    }

    @Test
    fun `존재하지 않는 닉네임이면 자동 생성 후 JWT를 발급한다`() {
        // given
        val newMember = Member(nickname = "newbie", role = MemberRole.USER, id = 2L)
        every { memberRepository.findByNickname("newbie") } returns Optional.empty()
        every { memberRepository.save(any()) } returns newMember
        every { jwtProvider.createToken(2L, "USER") } returns "new-token"

        // when
        val result = authService.login(LoginRequest(nickname = "newbie"))

        // then
        assertEquals("new-token", result.accessToken)
        assertEquals(2L, result.memberId)
        verify { memberRepository.save(any()) }
    }
}
