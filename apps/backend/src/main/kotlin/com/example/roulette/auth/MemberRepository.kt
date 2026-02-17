package com.example.roulette.auth

import com.example.roulette.auth.entity.Member
import com.example.roulette.auth.entity.MemberRole
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MemberRepository : JpaRepository<Member, Long> {
    fun findByNickname(nickname: String): Optional<Member>

    fun findAllByRole(role: MemberRole): List<Member>
}
