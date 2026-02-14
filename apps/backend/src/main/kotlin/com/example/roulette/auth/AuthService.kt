package com.example.roulette.auth

import com.example.roulette.auth.dto.LoginRequest
import com.example.roulette.auth.dto.LoginResponse
import com.example.roulette.auth.entity.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val memberRepository: MemberRepository,
    private val jwtProvider: JwtProvider,
) {

    /** 닉네임으로 로그인 (없으면 자동 생성) */
    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val member = memberRepository.findByNickname(request.nickname)
            .orElseGet { memberRepository.save(Member(nickname = request.nickname)) }

        val token = jwtProvider.createToken(member.id, member.role.name)

        return LoginResponse(
            accessToken = token,
            memberId = member.id,
            nickname = member.nickname,
            role = member.role.name,
        )
    }
}
