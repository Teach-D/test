package com.example.roulette.roulette.dto

import java.time.LocalDate

data class RouletteResultResponse(
    val point: Int,
    val playedAt: LocalDate,
)

data class RouletteStatusResponse(
    val hasPlayedToday: Boolean,
    val remainingBudget: Int,
    val canPlay: Boolean,
)
