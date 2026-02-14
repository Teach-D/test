package com.example.roulette.point.entity

import com.example.roulette.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "points")
class Point(
    @Column(nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    var remainingAmount: Int,

    @Column(nullable = false)
    val earnedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity() {

    /** 만료 여부 */
    val isExpired: Boolean
        get() = LocalDateTime.now().isAfter(expiresAt)

    /** 사용 가능한 포인트가 있는지 */
    val hasRemaining: Boolean
        get() = remainingAmount > 0 && !isExpired

    /** 포인트 차감 (사용 가능한 만큼만) */
    fun deduct(amount: Int): Int {
        val deducted = minOf(amount, remainingAmount)
        remainingAmount -= deducted
        return deducted
    }

    /** 포인트 환불 */
    fun restore(amount: Int) {
        remainingAmount = minOf(remainingAmount + amount, this.amount)
    }

    companion object {
        const val EXPIRY_DAYS = 30L
        const val EXPIRING_SOON_DAYS = 7L
    }
}
