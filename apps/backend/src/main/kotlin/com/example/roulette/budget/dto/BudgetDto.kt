package com.example.roulette.budget.dto

import jakarta.validation.constraints.Min
import java.time.LocalDate

data class BudgetResponse(
    val date: LocalDate,
    val totalBudget: Int,
    val usedBudget: Int,
    val remainingBudget: Int,
)

data class BudgetSetRequest(
    @field:Min(value = 0, message = "예산은 0 이상이어야 합니다.")
    val totalBudget: Int,
)
