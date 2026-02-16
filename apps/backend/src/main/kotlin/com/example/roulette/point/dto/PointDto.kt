package com.example.roulette.point.dto

import java.time.LocalDateTime

data class PointResponse(
    val id: Long,
    val amount: Int,
    val remainingAmount: Int,
    val earnedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val isExpired: Boolean,
    val isRevoked: Boolean,
)

data class PointBalanceResponse(
    val totalBalance: Int,
    val expiringSoonBalance: Int,
)
