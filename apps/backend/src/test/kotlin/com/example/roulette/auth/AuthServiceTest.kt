package com.example.roulette.auth

import com.example.roulette.auth.dto.LoginRequest
import com.example.roulette.auth.entity.Member
import com.example.roulette.auth.entity.MemberRole
import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

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

    // ==================== login ====================

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
    fun `존재하지 않는 닉네임이면 USER 역할로 자동 생성 후 JWT를 발급한다`() {
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
        assertEquals("USER", result.role)
        verify { memberRepository.save(match { it.role == MemberRole.USER }) }
    }

    @Test
    fun `신규 회원은 닉네임이 admin이어도 항상 USER 역할로 생성된다`() {
        // given — DB에 없는 신규 회원
        val newMember = Member(nickname = "admin", role = MemberRole.USER, id = 3L)
        every { memberRepository.findByNickname("admin") } returns Optional.empty()
        every { memberRepository.save(any()) } returns newMember
        every { jwtProvider.createToken(3L, "USER") } returns "user-token"

        // when
        val result = authService.login(LoginRequest(nickname = "admin"))

        // then
        assertEquals("USER", result.role)
        verify { memberRepository.save(match { it.role == MemberRole.USER }) }
    }

    @Test
    fun `기존 ADMIN 회원으로 로그인하면 DB의 ADMIN 역할을 그대로 반환한다`() {
        // given — DB에 이미 ADMIN으로 저장된 회원
        val adminMember = Member(nickname = "admin", role = MemberRole.ADMIN, id = 4L)
        every { memberRepository.findByNickname("admin") } returns Optional.of(adminMember)
        every { jwtProvider.createToken(4L, "ADMIN") } returns "admin-token"

        // when
        val result = authService.login(LoginRequest(nickname = "admin"))

        // then
        assertEquals("admin-token", result.accessToken)
        assertEquals("ADMIN", result.role)
    }

    // ==================== createAdmin ====================

    @Test
    fun `존재하지 않는 닉네임으로 관리자 생성 시 ADMIN 역할로 새 회원을 만든다`() {
        // given
        val newMember = Member(nickname = "newAdmin", role = MemberRole.USER, id = 10L)
        every { memberRepository.findByNickname("newAdmin") } returns Optional.empty()
        every { memberRepository.save(any()) } returns newMember

        // when
        val result = authService.createAdmin("newAdmin")

        // then
        assertEquals("newAdmin", result.nickname)
        assertEquals("ADMIN", result.role)
        verify { memberRepository.save(match { it.role == MemberRole.USER }) }
    }

    @Test
    fun `기존 USER 회원을 ADMIN으로 승격한다`() {
        // given
        val userMember = Member(nickname = "user1", role = MemberRole.USER, id = 5L)
        every { memberRepository.findByNickname("user1") } returns Optional.of(userMember)

        // when
        val result = authService.createAdmin("user1")

        // then
        assertEquals("ADMIN", result.role)
        assertEquals(MemberRole.ADMIN, userMember.role)
    }

    @Test
    fun `이미 ADMIN인 회원을 createAdmin 호출해도 정상 반환한다`() {
        // given
        val adminMember = Member(nickname = "existing", role = MemberRole.ADMIN, id = 6L)
        every { memberRepository.findByNickname("existing") } returns Optional.of(adminMember)

        // when
        val result = authService.createAdmin("existing")

        // then
        assertEquals("ADMIN", result.role)
    }

    // ==================== getAdminMembers ====================

    @Test
    fun `ADMIN 역할 회원 목록을 반환한다`() {
        // given
        val admins =
            listOf(
                Member(nickname = "admin1", role = MemberRole.ADMIN, id = 1L),
                Member(nickname = "admin2", role = MemberRole.ADMIN, id = 2L),
            )
        every { memberRepository.findAllByRole(MemberRole.ADMIN) } returns admins

        // when
        val result = authService.getAdminMembers()

        // then
        assertEquals(2, result.size)
        assertEquals("admin1", result[0].nickname)
        assertEquals("ADMIN", result[0].role)
        assertEquals("admin2", result[1].nickname)
    }

    @Test
    fun `ADMIN 회원이 없으면 빈 목록을 반환한다`() {
        // given
        every { memberRepository.findAllByRole(MemberRole.ADMIN) } returns emptyList()

        // when
        val result = authService.getAdminMembers()

        // then
        assertEquals(0, result.size)
    }

    // ==================== demoteAdmin ====================

    @Test
    fun `다른 관리자의 권한을 USER로 강등한다`() {
        // given
        val adminMember = Member(nickname = "other", role = MemberRole.ADMIN, id = 20L)
        every { memberRepository.findById(20L) } returns Optional.of(adminMember)

        // when
        authService.demoteAdmin(adminId = 20L, requesterId = 1L)

        // then
        assertEquals(MemberRole.USER, adminMember.role)
    }

    @Test
    fun `자기 자신의 관리자 권한을 제거하려 하면 CANNOT_DEMOTE_SELF 예외를 던진다`() {
        // when & then
        val ex =
            assertThrows<BusinessException> {
                authService.demoteAdmin(adminId = 1L, requesterId = 1L)
            }
        assertEquals(ErrorCode.CANNOT_DEMOTE_SELF, ex.errorCode)
    }

    @Test
    fun `존재하지 않는 회원을 강등하려 하면 MEMBER_NOT_FOUND 예외를 던진다`() {
        // given
        every { memberRepository.findById(99L) } returns Optional.empty()

        // when & then
        val ex =
            assertThrows<BusinessException> {
                authService.demoteAdmin(adminId = 99L, requesterId = 1L)
            }
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, ex.errorCode)
    }
}