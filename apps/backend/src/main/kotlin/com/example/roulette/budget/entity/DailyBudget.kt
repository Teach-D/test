package com.example.roulette.budget.entity

import com.example.roulette.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "daily_budgets")
class DailyBudget(
    @Column(nullable = false, unique = true)
    val budgetDate: LocalDate,
    @Column(nullable = false)
    var totalBudget: Int = DEFAULT_BUDGET,
    @Column(nullable = false)
    var usedBudget: Int = 0,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity() {
    /** 잔여 예산 */
    val remainingBudget: Int
        get() = totalBudget - usedBudget

    /** 예산 사용 가능 여부 */
    fun canUse(amount: Int): Boolean = remainingBudget >= amount

    /** 예산 사용 */
    fun use(amount: Int) {
        usedBudget += amount
    }

    /** 예산 반환 (취소 시) */
    fun refund(amount: Int) {
        usedBudget = (usedBudget - amount).coerceAtLeast(0)
    }

    companion object {
        const val DEFAULT_BUDGET = 100_000
    }
}
