package com.example.roulette.order.entity

import com.example.roulette.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class Order(
    @Column(nullable = false)
    val memberId: Long,
    @Column(nullable = false)
    val productId: Long,
    @Column(nullable = false)
    val usedPoint: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.COMPLETED,
    @Column(nullable = false)
    val orderedAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity() {
    fun cancel() {
        status = OrderStatus.CANCELLED
    }
}

enum class OrderStatus {
    COMPLETED,
    CANCELLED,
}
