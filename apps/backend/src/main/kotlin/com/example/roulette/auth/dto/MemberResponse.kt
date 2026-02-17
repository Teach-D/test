package com.example.roulette.auth.dto

import com.example.roulette.auth.entity.Member
import java.time.LocalDateTime

data class MemberResponse(
    val id: Long,
    val nickname: String,
    val role: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(member: Member): MemberResponse =
            MemberResponse(
                id = member.id,
                nickname = member.nickname,
                role = member.role.name,
                createdAt = member.createdAt,
            )
    }
}
