package com.example.roulette.point

import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.point.entity.Point
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class PointServiceTest {
    private lateinit var pointRepository: PointRepository
    private lateinit var pointService: PointService

    @BeforeEach
    fun setUp() {
        pointRepository = mockk(relaxed = true)
        pointService = PointService(pointRepository)
    }

    @Test
    fun `포인트를 지급하면 30일 후 만료로 저장된다`() {
        // given
        val memberId = 1L
        val amount = 500
        val savedPoint =
            Point(
                memberId = memberId,
                amount = amount,
                remainingAmount = amount,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 1L,
            )
        every { pointRepository.save(any()) } returns savedPoint

        // when
        val result = pointService.grant(memberId, amount)

        // then
        assertEquals(amount, result.amount)
        assertEquals(amount, result.remainingAmount)
    }

    @Test
    fun `포인트 차감 시 만료 임박 순으로 FIFO 차감한다`() {
        // given
        val memberId = 1L
        val now = LocalDateTime.now()
        val point1 =
            Point(
                memberId = memberId,
                amount = 300,
                remainingAmount = 300,
                expiresAt = now.plusDays(5),
                id = 1L,
            )
        val point2 =
            Point(
                memberId = memberId,
                amount = 500,
                remainingAmount = 500,
                expiresAt = now.plusDays(20),
                id = 2L,
            )

        every { pointRepository.getAvailableBalance(memberId, any()) } returns 800
        every { pointRepository.findAvailablePoints(memberId, any()) } returns listOf(point1, point2)

        // when
        val usages = pointService.deduct(memberId, 400)

        // then
        assertEquals(2, usages.size)
        assertEquals(1L to 300, usages[0]) // point1에서 300 전부 차감
        assertEquals(2L to 100, usages[1]) // point2에서 100 차감
        assertEquals(0, point1.remainingAmount)
        assertEquals(400, point2.remainingAmount)
    }

    @Test
    fun `잔액 부족 시 BusinessException을 던진다`() {
        // given
        val memberId = 1L
        every { pointRepository.getAvailableBalance(memberId, any()) } returns 100

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                pointService.deduct(memberId, 500)
            }
        assertEquals(ErrorCode.POINT_NOT_ENOUGH, exception.errorCode)
    }

    @Test
    fun `포인트 잔액 조회 시 만료되지 않은 포인트만 합산한다`() {
        // given
        val memberId = 1L
        val now = LocalDateTime.now()
        every { pointRepository.getAvailableBalance(memberId, any()) } returns 1500
        every { pointRepository.findExpiringSoon(memberId, any(), any()) } returns
            listOf(
                Point(memberId = memberId, amount = 300, remainingAmount = 200, expiresAt = now.plusDays(3), id = 1L),
            )

        // when
        val result = pointService.getBalance(memberId)

        // then
        assertEquals(1500, result.totalBalance)
        assertEquals(200, result.expiringSoonBalance)
    }

    @Test
    fun `포인트 환불 시 원래 금액까지만 복원된다`() {
        // given
        val point =
            Point(
                memberId = 1L,
                amount = 500,
                remainingAmount = 200,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 1L,
            )
        every { pointRepository.findById(1L) } returns Optional.of(point)

        // when
        pointService.restore(listOf(1L to 300))

        // then
        assertEquals(500, point.remainingAmount)
    }

    @Test
    fun `포인트 환불 시 원래 금액을 초과하면 원래 금액으로 제한된다`() {
        // given
        val point =
            Point(
                memberId = 1L,
                amount = 500,
                remainingAmount = 400,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 1L,
            )
        every { pointRepository.findById(1L) } returns Optional.of(point)

        // when
        pointService.restore(listOf(1L to 300))

        // then
        assertEquals(500, point.remainingAmount) // 400 + 300 = 700이지만, 원래 금액 500으로 제한
    }

    @Test
    fun `포인트를 회수하면 잔액이 0이 되고 isRevoked가 true가 된다`() {
        // given
        val point =
            Point(
                memberId = 1L,
                amount = 500,
                remainingAmount = 500,
                expiresAt = LocalDateTime.now().plusDays(30),
                id = 1L,
            )
        every { pointRepository.findById(1L) } returns Optional.of(point)

        // when
        pointService.revoke(1L)

        // then
        assertEquals(0, point.remainingAmount)
        assertTrue(point.isRevoked)
    }

    @Test
    fun `존재하지 않는 포인트를 회수하면 POINT_NOT_FOUND 예외를 던진다`() {
        // given
        every { pointRepository.findById(999L) } returns Optional.empty()

        // when & then
        val exception =
            assertThrows(BusinessException::class.java) {
                pointService.revoke(999L)
            }
        assertEquals(ErrorCode.POINT_NOT_FOUND, exception.errorCode)
    }

    @Test
    fun `포인트 내역을 최신순으로 조회할 수 있다`() {
        // given
        val memberId = 1L
        val now = LocalDateTime.now()
        val points =
            listOf(
                Point(memberId = memberId, amount = 300, remainingAmount = 300, expiresAt = now.plusDays(30), id = 1L),
                Point(memberId = memberId, amount = 500, remainingAmount = 200, expiresAt = now.plusDays(20), id = 2L),
            )
        every { pointRepository.findByMemberIdOrderByEarnedAtDesc(memberId) } returns points

        // when
        val result = pointService.getPoints(memberId)

        // then
        assertEquals(2, result.size)
        assertEquals(1L, result[0].id)
        assertEquals(300, result[0].amount)
        assertEquals(300, result[0].remainingAmount)
        assertFalse(result[0].isRevoked)
        assertEquals(2L, result[1].id)
        assertEquals(500, result[1].amount)
        assertEquals(200, result[1].remainingAmount)
    }

    @Test
    fun `만료 예정 포인트를 조회할 수 있다`() {
        // given
        val memberId = 1L
        val now = LocalDateTime.now()
        val expiringSoonPoints =
            listOf(
                Point(memberId = memberId, amount = 200, remainingAmount = 150, expiresAt = now.plusDays(3), id = 3L),
            )
        every { pointRepository.findExpiringSoon(memberId, any(), any()) } returns expiringSoonPoints

        // when
        val result = pointService.getExpiringSoon(memberId)

        // then
        assertEquals(1, result.size)
        assertEquals(3L, result[0].id)
        assertEquals(200, result[0].amount)
        assertEquals(150, result[0].remainingAmount)
        assertFalse(result[0].isRevoked)
    }
}
