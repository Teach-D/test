package com.example.roulette.budget

import com.example.roulette.budget.dto.BudgetResponse
import com.example.roulette.budget.dto.BudgetSetRequest
import com.example.roulette.budget.entity.DailyBudget
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class BudgetService(
    private val budgetRepository: BudgetRepository,
) {

    /** 오늘 예산 조회 (없으면 기본값으로 생성) */
    @Transactional(readOnly = true)
    fun getTodayBudget(): BudgetResponse {
        val budget = getOrCreateBudget(LocalDate.now())
        return toResponse(budget)
    }

    /** 오늘 예산 설정 (어드민) */
    @Transactional
    fun setTodayBudget(request: BudgetSetRequest): BudgetResponse {
        val today = LocalDate.now()
        val budget = budgetRepository.findByBudgetDate(today)
            .orElseGet { DailyBudget(budgetDate = today) }
        budget.totalBudget = request.totalBudget
        val saved = budgetRepository.save(budget)
        return toResponse(saved)
    }

    /** 오늘 예산 조회/생성 (내부용) */
    @Transactional
    fun getOrCreateBudget(date: LocalDate): DailyBudget {
        return budgetRepository.findByBudgetDate(date)
            .orElseGet { budgetRepository.save(DailyBudget(budgetDate = date)) }
    }

    private fun toResponse(budget: DailyBudget) = BudgetResponse(
        date = budget.budgetDate,
        totalBudget = budget.totalBudget,
        usedBudget = budget.usedBudget,
        remainingBudget = budget.remainingBudget,
    )
}
