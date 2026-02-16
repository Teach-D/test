package com.example.roulette.budget

import com.example.roulette.budget.dto.BudgetSetRequest
import com.example.roulette.budget.entity.DailyBudget
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.*

class BudgetServiceTest {
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var budgetService: BudgetService

    @BeforeEach
    fun setUp() {
        budgetRepository = mockk()
        budgetService = BudgetService(budgetRepository)
    }

    @Test
    fun `오늘 예산이 없으면 기본값으로 생성한다`() {
        // given
        val today = LocalDate.now()
        val defaultBudget = DailyBudget(budgetDate = today, id = 1L)
        every { budgetRepository.findByBudgetDate(today) } returns Optional.empty()
        every { budgetRepository.save(any()) } returns defaultBudget

        // when
        val result = budgetService.getTodayBudget()

        // then
        assertEquals(today, result.date)
        assertEquals(100_000, result.totalBudget)
        assertEquals(0, result.usedBudget)
        assertEquals(100_000, result.remainingBudget)
    }

    @Test
    fun `예산을 설정하면 값이 변경된다`() {
        // given
        val today = LocalDate.now()
        val budget = DailyBudget(budgetDate = today, totalBudget = 100_000, id = 1L)
        every { budgetRepository.findByBudgetDate(today) } returns Optional.of(budget)
        val savedSlot = slot<DailyBudget>()
        every { budgetRepository.save(capture(savedSlot)) } answers { savedSlot.captured }

        // when
        val result = budgetService.setTodayBudget(BudgetSetRequest(totalBudget = 200_000))

        // then
        assertEquals(200_000, result.totalBudget)
    }
}
