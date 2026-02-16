package com.example.roulette.roulette

import com.example.roulette.budget.BudgetRepository
import com.example.roulette.budget.BudgetService
import com.example.roulette.budget.entity.DailyBudget
import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.point.PointService
import com.example.roulette.roulette.dto.RouletteHistoryResponse
import com.example.roulette.roulette.dto.RouletteResultResponse
import com.example.roulette.roulette.dto.RouletteStatusResponse
import com.example.roulette.roulette.entity.RouletteHistory
import org.springframework.dao.DataIntegrityViolationException
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

        // 2. 예산 조회 및 락 획득
        val budget = getOrCreateBudgetWithLock(today)

        // 3. 잔여 예산 확인
        val remainingBudget = budget.remainingBudget
        if (remainingBudget < MIN_POINT) {
            throw BusinessException(ErrorCode.BUDGET_EXCEEDED)
        }

        // 4. 잔여 예산 범위 내에서 랜덤 포인트 생성 (100~min(1000, 잔여예산), 100 단위)
        val maxPoint = minOf(MAX_POINT, (remainingBudget / POINT_UNIT) * POINT_UNIT)
        val point = generateRandomPointInRange(MIN_POINT, maxPoint)

        // 5. 예산 차감
        budget.use(point)

        // 6. 룰렛 기록 저장 (UNIQUE 제약으로 동시 중복 참여 방지)
        try {
            rouletteRepository.saveAndFlush(
                RouletteHistory(memberId = memberId, point = point, playedAt = today),
            )
        } catch (e: DataIntegrityViolationException) {
            throw BusinessException(ErrorCode.ROULETTE_ALREADY_PLAYED)
        }

        // 7. 포인트 지급
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
        val history =
            rouletteRepository
                .findById(historyId)
                .orElseThrow { BusinessException(ErrorCode.ROULETTE_NOT_FOUND) }

        if (history.isCancelled) {
            throw BusinessException(ErrorCode.ROULETTE_ALREADY_CANCELLED)
        }

        // 1. 룰렛 기록 취소
        history.cancel()

        // 2. 예산 반환
        val budget =
            budgetRepository
                .findByBudgetDateWithLock(history.playedAt)
                .orElseThrow { BusinessException(ErrorCode.BUDGET_NOT_FOUND) }
        budget.refund(history.point)

        // 3. 포인트 회수 — 해당 회원의 해당 금액 포인트 찾아서 잔액 0으로
        val points = pointService.getPointsByMemberAndAmount(history.memberId, history.point, history.createdAt)
        points.firstOrNull()?.let { pointService.revoke(it.id) }
    }

    /** 전체 룰렛 참여 내역 조회 (어드민) */
    @Transactional(readOnly = true)
    fun getAllHistories(): List<RouletteHistoryResponse> =
        rouletteRepository
            .findAllByOrderByPlayedAtDesc()
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

    /** 비관적 락으로 예산 조회, 없으면 생성 후 락 재획득 */
    private fun getOrCreateBudgetWithLock(date: LocalDate): DailyBudget =
        budgetRepository
            .findByBudgetDateWithLock(date)
            .orElseGet {
                try {
                    budgetRepository.saveAndFlush(DailyBudget(budgetDate = date))
                } catch (e: DataIntegrityViolationException) {
                    // 다른 트랜잭션이 먼저 생성한 경우 무시
                }
                budgetRepository
                    .findByBudgetDateWithLock(date)
                    .orElseThrow { BusinessException(ErrorCode.BUDGET_NOT_FOUND) }
            }

    private fun generateRandomPointInRange(
        min: Int,
        max: Int,
    ): Int = (Random.nextInt(min / POINT_UNIT, max / POINT_UNIT + 1)) * POINT_UNIT

    companion object {
        const val MIN_POINT = 100
        const val MAX_POINT = 1000
        const val POINT_UNIT = 100
    }
}
