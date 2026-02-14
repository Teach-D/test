package com.example.roulette.order.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "point_usages")
class PointUsage(
    @Column(nullable = false)
    val pointId: Long,

    @Column(nullable = false)
    val orderId: Long,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
)
