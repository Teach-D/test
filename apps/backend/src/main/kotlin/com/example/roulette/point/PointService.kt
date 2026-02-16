package com.example.roulette.point

import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.point.dto.PointBalanceResponse
import com.example.roulette.point.dto.PointResponse
import com.example.roulette.point.entity.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PointService(
    private val pointRepository: PointRepository,
) {
    /** 포인트 지급 (룰렛 당첨 시) */
    @Transactional
    fun grant(
        memberId: Long,
        amount: Int,
    ): Point {
        val now = LocalDateTime.now()
        val point =
            Point(
                memberId = memberId,
                amount = amount,
                remainingAmount = amount,
                earnedAt = now,
                expiresAt = now.plusDays(Point.EXPIRY_DAYS),
            )
        return pointRepository.save(point)
    }

    /** 포인트 차감 (만료 임박순 FIFO) */
    @Transactional
    fun deduct(
        memberId: Long,
        totalAmount: Int,
    ): List<Pair<Long, Int>> {
        val now = LocalDateTime.now()
        val balance = pointRepository.getAvailableBalance(memberId, now)
        if (balance < totalAmount) {
            throw BusinessException(ErrorCode.POINT_NOT_ENOUGH)
        }

        val availablePoints = pointRepository.findAvailablePoints(memberId, now)
        var remaining = totalAmount
        val usages = mutableListOf<Pair<Long, Int>>() // pointId to deductedAmount

        for (point in availablePoints) {
            if (remaining <= 0) break
            val deducted = point.deduct(remaining)
            if (deducted > 0) {
                usages.add(point.id to deducted)
                remaining -= deducted
            }
        }

        return usages
    }

    /** 포인트 환불 (주문 취소 시) */
    @Transactional
    fun restore(pointUsages: List<Pair<Long, Int>>) {
        for ((pointId, amount) in pointUsages) {
            val point =
                pointRepository
                    .findById(pointId)
                    .orElseThrow { BusinessException(ErrorCode.POINT_NOT_FOUND) }
            point.restore(amount)
        }
    }

    /** 포인트 회수 (룰렛 취소 시) — 해당 포인트의 잔액을 0으로 */
    @Transactional
    fun revoke(pointId: Long) {
        val point =
            pointRepository
                .findById(pointId)
                .orElseThrow { BusinessException(ErrorCode.POINT_NOT_FOUND) }
        point.remainingAmount = 0
    }

    /** 회원의 특정 금액/시간 이후 생성된 포인트 조회 (룰렛 취소 시 사용) */
    @Transactional(readOnly = true)
    fun getPointsByMemberAndAmount(
        memberId: Long,
        amount: Int,
        afterTime: LocalDateTime,
    ): List<Point> = pointRepository.findByMemberIdAndAmountAndEarnedAtAfter(memberId, amount, afterTime)

    /** 내 포인트 잔액 조회 */
    @Transactional(readOnly = true)
    fun getBalance(memberId: Long): PointBalanceResponse {
        val now = LocalDateTime.now()
        val totalBalance = pointRepository.getAvailableBalance(memberId, now)
        val expiringSoon =
            pointRepository.findExpiringSoon(
                memberId,
                now,
                now.plusDays(Point.EXPIRING_SOON_DAYS),
            )
        val expiringSoonBalance = expiringSoon.sumOf { it.remainingAmount }

        return PointBalanceResponse(
            totalBalance = totalBalance,
            expiringSoonBalance = expiringSoonBalance,
        )
    }

    /** 내 포인트 목록 조회 */
    @Transactional(readOnly = true)
    fun getPoints(memberId: Long): List<PointResponse> =
        pointRepository
            .findByMemberIdOrderByEarnedAtDesc(memberId)
            .map { toResponse(it) }

    /** 만료 예정 포인트 조회 (7일 이내) */
    @Transactional(readOnly = true)
    fun getExpiringSoon(memberId: Long): List<PointResponse> {
        val now = LocalDateTime.now()
        return pointRepository
            .findExpiringSoon(
                memberId,
                now,
                now.plusDays(Point.EXPIRING_SOON_DAYS),
            ).map { toResponse(it) }
    }

    private fun toResponse(point: Point) =
        PointResponse(
            id = point.id,
            amount = point.amount,
            remainingAmount = point.remainingAmount,
            earnedAt = point.earnedAt,
            expiresAt = point.expiresAt,
            isExpired = point.isExpired,
        )
}
