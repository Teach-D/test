package com.example.roulette.auth

import com.example.roulette.auth.dto.LoginRequest
import com.example.roulette.auth.dto.LoginResponse
import com.example.roulette.auth.dto.MemberResponse
import com.example.roulette.auth.entity.Member
import com.example.roulette.auth.entity.MemberRole
import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val jwtProvider: JwtProvider,
) {
    /**
     * 닉네임으로 로그인한다.
     * 존재하지 않는 닉네임이면 USER 역할로 자동 생성한다.
     * 기존 회원은 DB에 저장된 role을 그대로 사용한다.
     */
    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val member =
            memberRepository
                .findByNickname(request.nickname)
                .orElseGet { memberRepository.save(Member(nickname = request.nickname)) }

        val token = jwtProvider.createToken(member.id, member.role.name)

        return LoginResponse(
            accessToken = token,
            memberId = member.id,
            nickname = member.nickname,
            role = member.role.name,
        )
    }

    /**
     * 닉네임으로 ADMIN 계정을 생성하거나 기존 회원을 ADMIN으로 승격한다.
     * 이미 ADMIN인 경우에도 정상 반환한다.
     */
    @Transactional
    fun createAdmin(nickname: String): MemberResponse {
        val member =
            memberRepository
                .findByNickname(nickname)
                .orElseGet { memberRepository.save(Member(nickname = nickname)) }

        member.changeRole(MemberRole.ADMIN)

        return MemberResponse.from(member)
    }

    /**
     * ADMIN 역할을 가진 회원 목록을 반환한다.
     */
    @Transactional(readOnly = true)
    fun getAdminMembers(): List<MemberResponse> =
        memberRepository
            .findAllByRole(MemberRole.ADMIN)
            .map { MemberResponse.from(it) }

    /**
     * 관리자 권한을 USER로 강등한다.
     * 자기 자신의 권한은 제거할 수 없다.
     */
    @Transactional
    fun demoteAdmin(
        adminId: Long,
        requesterId: Long,
    ) {
        if (adminId == requesterId) {
            throw BusinessException(ErrorCode.CANNOT_DEMOTE_SELF)
        }

        val member =
            memberRepository.findById(adminId).orElseThrow {
                BusinessException(ErrorCode.MEMBER_NOT_FOUND)
            }

        member.changeRole(MemberRole.USER)
    }
}
