package com.example.roulette.roulette.dto

import java.time.LocalDate
import java.time.LocalDateTime

data class RouletteResultResponse(
    val point: Int,
    val playedAt: LocalDate,
)

data class RouletteStatusResponse(
    val hasPlayedToday: Boolean,
    val remainingBudget: Int,
    val canPlay: Boolean,
)

data class RouletteHistoryResponse(
    val id: Long,
    val memberId: Long,
    val point: Int,
    val isCancelled: Boolean,
    val playedAt: LocalDate,
    val createdAt: LocalDateTime,
)
