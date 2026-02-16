package com.example.roulette.roulette

import com.example.roulette.budget.BudgetRepository
import com.example.roulette.budget.BudgetService
import com.example.roulette.budget.entity.DailyBudget
import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.point.PointService
import com.example.roulette.point.entity.Point
import com.example.roulette.roulette.entity.RouletteHistory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class RouletteServiceTest {
    private lateinit var rouletteRepository: RouletteRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var budgetService: BudgetService
    private lateinit var pointService: PointService
    private lateinit var rouletteService: RouletteService

    @BeforeEach
    fun setUp() {
        rouletteRepository = mockk(relaxed = true)
        budgetRepository = mockk()
        budgetService = mockk()
        pointService = mockk(relaxed = true)
        rouletteService = RouletteService(rouletteRepository, budgetRepository, budgetService, pointService)
    }

    @Test
    fun `룰렛을 돌리면 100~1000 포인트를 획득한다`() {
        // given
        val memberId = 1L
        val today = LocalDate.now()
        val budget = DailyBudget(budgetDate = today, totalBudget = 100_000, id = 1L)

        every { rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today) } returns false
        every { budgetRepository.findByBudgetDateWithLock(today) } returns Optional.of(budget)
        every { rouletteRepository.saveAndFlush(any()) } answers { firstArg() }
        every { pointService.grant(any(), any()) } returns
            Point(
                memberId = memberId,
                amount = 100,
                remainingAmount = 100,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 1L,
            )

        // when
        val result = rouletteService.spin(memberId)

        // then
        assertTrue(result.point in 100..1000)
        assertEquals(0, result.point % 100)
        assertEquals(today, result.playedAt)
    }

    @Test
    fun `이미 오늘 참여했으면 BusinessException을 던진다`() {
        // given
        val memberId = 1L
        val today = LocalDate.now()
        every { rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today) } returns true

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                rouletteService.spin(memberId)
            }
        assertEquals(ErrorCode.ROULETTE_ALREADY_PLAYED, exception.errorCode)
    }

    @Test
    fun `잔여 예산이 100p 미만이면 BusinessException을 던진다`() {
        // given
        val memberId = 1L
        val today = LocalDate.now()
        val budget = DailyBudget(budgetDate = today, totalBudget = 100_000, usedBudget = 99_950, id = 1L)

        every { rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today) } returns false
        every { budgetRepository.findByBudgetDateWithLock(today) } returns Optional.of(budget)

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                rouletteService.spin(memberId)
            }
        assertEquals(ErrorCode.BUDGET_EXCEEDED, exception.errorCode)
    }

    @Test
    fun `잔여 예산이 300p이면 100~300 범위의 포인트를 획득한다`() {
        // given
        val memberId = 1L
        val today = LocalDate.now()
        val budget = DailyBudget(budgetDate = today, totalBudget = 100_000, usedBudget = 99_700, id = 1L)

        every { rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today) } returns false
        every { budgetRepository.findByBudgetDateWithLock(today) } returns Optional.of(budget)
        every { rouletteRepository.saveAndFlush(any()) } answers { firstArg() }
        every { pointService.grant(any(), any()) } returns
            Point(
                memberId = memberId,
                amount = 100,
                remainingAmount = 100,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 1L,
            )

        // when
        val result = rouletteService.spin(memberId)

        // then
        assertTrue(result.point in 100..300)
        assertEquals(0, result.point % 100)
        assertEquals(today, result.playedAt)
    }

    @Test
    fun `룰렛 취소 시 예산이 반환되고 포인트가 회수된다`() {
        // given
        val historyId = 1L
        val today = LocalDate.now()
        val history = RouletteHistory(memberId = 1L, point = 500, playedAt = today, id = historyId)
        val budget = DailyBudget(budgetDate = today, totalBudget = 100_000, usedBudget = 500, id = 1L)
        val point =
            Point(
                memberId = 1L,
                amount = 500,
                remainingAmount = 500,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 10L,
            )

        every { rouletteRepository.findById(historyId) } returns Optional.of(history)
        every { budgetRepository.findByBudgetDateWithLock(today) } returns Optional.of(budget)
        every { pointService.getPointsByMemberAndAmount(any(), any(), any()) } returns listOf(point)
        every { pointService.revoke(any()) } returns Unit

        // when
        rouletteService.cancel(historyId)

        // then
        assertTrue(history.isCancelled)
        assertEquals(0, budget.usedBudget)
        verify { pointService.revoke(10L) }
    }

    @Test
    fun `존재하지 않는 룰렛 내역을 취소하면 ROULETTE_NOT_FOUND 예외를 던진다`() {
        // given
        val historyId = 999L
        every { rouletteRepository.findById(historyId) } returns Optional.empty()

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                rouletteService.cancel(historyId)
            }
        assertEquals(ErrorCode.ROULETTE_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `이미 취소된 룰렛을 다시 취소하면 ROULETTE_ALREADY_CANCELLED 예외를 던진다`() {
        // given
        val historyId = 1L
        val today = LocalDate.now()
        val history = RouletteHistory(memberId = 1L, point = 500, playedAt = today, id = historyId)
        history.cancel() // 이미 취소 처리

        every { rouletteRepository.findById(historyId) } returns Optional.of(history)

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                rouletteService.cancel(historyId)
            }
        assertEquals(ErrorCode.ROULETTE_ALREADY_CANCELLED, exception.errorCode)
    }

    @Test
    fun `오늘 참여 상태를 조회할 수 있다`() {
        // given
        val memberId = 1L
        val today = LocalDate.now()
        val budget = DailyBudget(budgetDate = today, totalBudget = 100_000, usedBudget = 0, id = 1L)

        every { rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today) } returns false
        every { budgetService.getOrCreateBudget(today) } returns budget

        // when
        val result = rouletteService.getStatus(memberId)

        // then
        assertFalse(result.hasPlayedToday)
        assertEquals(100_000, result.remainingBudget)
        assertTrue(result.canPlay)
    }

    @Test
    fun `전체 참여 내역을 조회할 수 있다`() {
        // given
        val today = LocalDate.now()
        val histories =
            listOf(
                RouletteHistory(memberId = 1L, point = 300, playedAt = today, id = 1L),
                RouletteHistory(memberId = 2L, point = 500, playedAt = today.minusDays(1), id = 2L),
            )
        every { rouletteRepository.findAllByOrderByPlayedAtDesc() } returns histories

        // when
        val result = rouletteService.getAllHistories()

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(1L, result[0].memberId)
        assertEquals(300, result[0].point)
        assertFalse(result[0].isCancelled)
        assertEquals(today, result[0].playedAt)
        assertEquals(2L, result[1].id)
        assertEquals(500, result[1].point)
    }
}
