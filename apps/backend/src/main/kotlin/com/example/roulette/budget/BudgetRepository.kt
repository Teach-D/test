package com.example.roulette.budget

import com.example.roulette.budget.entity.DailyBudget
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.util.Optional

interface BudgetRepository : JpaRepository<DailyBudget, Long> {

    fun findByBudgetDate(budgetDate: LocalDate): Optional<DailyBudget>

    /** 비관적 락으로 예산 조회 (동시성 제어) */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM DailyBudget b WHERE b.budgetDate = :budgetDate")
    fun findByBudgetDateWithLock(budgetDate: LocalDate): Optional<DailyBudget>
}
