package com.example.roulette.roulette

import com.example.roulette.budget.BudgetRepository
import com.example.roulette.budget.BudgetService
import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.point.PointService
import com.example.roulette.point.entity.Point
import com.example.roulette.roulette.dto.RouletteHistoryResponse
import com.example.roulette.roulette.dto.RouletteResultResponse
import com.example.roulette.roulette.dto.RouletteStatusResponse
import com.example.roulette.roulette.entity.RouletteHistory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.random.Random

@Service
class RouletteService(
    private val rouletteRepository: RouletteRepository,
    private val budgetRepository: BudgetRepository,
    private val budgetService: BudgetService,
    private val pointService: PointService,
) {

    /** 룰렛 참여 */
    @Transactional
    fun spin(memberId: Long): RouletteResultResponse {
        val today = LocalDate.now()

        // 1. 중복 참여 체크
        if (rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today)) {
            throw BusinessException(ErrorCode.ROULETTE_ALREADY_PLAYED)
        }

        // 2. 랜덤 포인트 결정 (100~1000, 100 단위)
        val point = generateRandomPoint()

        // 3. 예산 확인 및 차감 (비관적 락)
        val budget = budgetRepository.findByBudgetDateWithLock(today)
            .orElseGet {
                budgetService.getOrCreateBudget(today).also {
                    budgetRepository.flush()
                }
            }

        if (!budget.canUse(point)) {
            throw BusinessException(ErrorCode.BUDGET_EXCEEDED)
        }
        budget.use(point)

        // 4. 룰렛 기록 저장
        rouletteRepository.save(
            RouletteHistory(memberId = memberId, point = point, playedAt = today)
        )

        // 5. 포인트 지급
        pointService.grant(memberId, point)

        return RouletteResultResponse(point = point, playedAt = today)
    }

    /** 오늘 참여 상태 확인 */
    @Transactional
    fun getStatus(memberId: Long): RouletteStatusResponse {
        val today = LocalDate.now()
        val hasPlayed = rouletteRepository.existsByMemberIdAndPlayedAtAndIsCancelledFalse(memberId, today)
        val budget = budgetService.getOrCreateBudget(today)

        return RouletteStatusResponse(
            hasPlayedToday = hasPlayed,
            remainingBudget = budget.remainingBudget,
            canPlay = !hasPlayed && budget.canUse(MIN_POINT),
        )
    }

    /** 룰렛 참여 취소 (어드민) */
    @Transactional
    fun cancel(historyId: Long) {
        val history = rouletteRepository.findById(historyId)
            .orElseThrow { BusinessException(ErrorCode.ROULETTE_NOT_FOUND) }

        if (history.isCancelled) {
            throw BusinessException(ErrorCode.ROULETTE_ALREADY_CANCELLED)
        }

        // 1. 룰렛 기록 취소
        history.cancel()

        // 2. 예산 반환
        val budget = budgetRepository.findByBudgetDateWithLock(history.playedAt)
            .orElseThrow { BusinessException(ErrorCode.BUDGET_NOT_FOUND) }
        budget.refund(history.point)

        // 3. 포인트 회수 — 해당 회원의 해당 금액 포인트 찾아서 잔액 0으로
        val points = pointService.getPointsByMemberAndAmount(history.memberId, history.point, history.createdAt)
        points.firstOrNull()?.let { pointService.revoke(it.id) }
    }

    /** 전체 룰렛 참여 내역 조회 (어드민) */
    @Transactional(readOnly = true)
    fun getAllHistories(): List<RouletteHistoryResponse> {
        return rouletteRepository.findAllByOrderByPlayedAtDesc()
            .map { history ->
                RouletteHistoryResponse(
                    id = history.id,
                    memberId = history.memberId,
                    point = history.point,
                    isCancelled = history.isCancelled,
                    playedAt = history.playedAt,
                    createdAt = history.createdAt,
                )
            }
    }

    private fun generateRandomPoint(): Int {
        return (Random.nextInt(MIN_POINT / POINT_UNIT, MAX_POINT / POINT_UNIT + 1)) * POINT_UNIT
    }

    companion object {
        const val MIN_POINT = 100
        const val MAX_POINT = 1000
        const val POINT_UNIT = 100
    }
}
