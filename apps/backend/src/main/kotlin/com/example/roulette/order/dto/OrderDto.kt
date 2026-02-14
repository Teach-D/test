package com.example.roulette.order.dto

import java.time.LocalDateTime

data class OrderRequest(
    val productId: Long,
)

data class OrderResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val usedPoint: Int,
    val status: String,
    val orderedAt: LocalDateTime,
)
