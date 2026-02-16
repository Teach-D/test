package com.example.roulette.roulette

import com.example.roulette.roulette.entity.RouletteHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

interface RouletteRepository : JpaRepository<RouletteHistory, Long> {
    /** 특정 회원의 특정 날짜 참여 기록 (취소되지 않은 것) */
    fun findByMemberIdAndPlayedAtAndIsCancelledFalse(
        memberId: Long,
        playedAt: LocalDate,
    ): Optional<RouletteHistory>

    /** 오늘 참여 여부 확인 */
    fun existsByMemberIdAndPlayedAtAndIsCancelledFalse(
        memberId: Long,
        playedAt: LocalDate,
    ): Boolean

    /** 전체 룰렛 참여 내역 조회 (어드민) */
    fun findAllByOrderByPlayedAtDesc(): List<RouletteHistory>

    /** 오늘 참여자 수 (취소되지 않은 것만) */
    fun countByPlayedAtAndIsCancelledFalse(playedAt: LocalDate): Int
}
