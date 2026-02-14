package com.example.roulette.point

import com.example.roulette.point.entity.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface PointRepository : JpaRepository<Point, Long> {

    /** 회원의 사용 가능한 포인트 목록 (만료일 오름차순 — FIFO 차감용) */
    @Query(
        "SELECT p FROM Point p WHERE p.memberId = :memberId " +
            "AND p.remainingAmount > 0 AND p.expiresAt > :now ORDER BY p.expiresAt ASC"
    )
    fun findAvailablePoints(memberId: Long, now: LocalDateTime = LocalDateTime.now()): List<Point>

    /** 회원의 전체 포인트 목록 (최신순) */
    fun findByMemberIdOrderByEarnedAtDesc(memberId: Long): List<Point>

    /** 만료 임박 포인트 (7일 이내 만료, 잔액 있는 것만) */
    @Query(
        "SELECT p FROM Point p WHERE p.memberId = :memberId " +
            "AND p.remainingAmount > 0 AND p.expiresAt > :now AND p.expiresAt <= :threshold " +
            "ORDER BY p.expiresAt ASC"
    )
    fun findExpiringSoon(memberId: Long, now: LocalDateTime, threshold: LocalDateTime): List<Point>

    /** 회원의 사용 가능한 총 잔액 */
    @Query(
        "SELECT COALESCE(SUM(p.remainingAmount), 0) FROM Point p " +
            "WHERE p.memberId = :memberId AND p.remainingAmount > 0 AND p.expiresAt > :now"
    )
    fun getAvailableBalance(memberId: Long, now: LocalDateTime = LocalDateTime.now()): Int

    /** 특정 회원의 특정 금액/시간 이후 포인트 조회 */
    fun findByMemberIdAndAmountAndEarnedAtAfter(
        memberId: Long,
        amount: Int,
        earnedAt: LocalDateTime,
    ): List<Point>
}
