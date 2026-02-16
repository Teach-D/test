package com.example.roulette.roulette

import com.example.roulette.auth.MemberRepository
import com.example.roulette.auth.entity.Member
import com.example.roulette.auth.entity.MemberRole
import com.example.roulette.budget.BudgetRepository
import com.example.roulette.budget.entity.DailyBudget
import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.point.PointRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ActiveProfiles("dev")
class RouletteConcurrencyIntegrationTest {
    @Autowired
    private lateinit var rouletteService: RouletteService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var budgetRepository: BudgetRepository

    @Autowired
    private lateinit var rouletteRepository: RouletteRepository

    @Autowired
    private lateinit var pointRepository: PointRepository

    private lateinit var testMember: Member
    private val today = LocalDate.now()

    @BeforeEach
    fun setUp() {
        // 테스트용 회원 생성
        testMember =
            memberRepository.save(
                Member(nickname = "concurrency_test_user_${System.nanoTime()}", role = MemberRole.USER),
            )
    }

    @AfterEach
    fun tearDown() {
        // 데이터 정리 (역순으로 삭제)
        pointRepository.deleteAll()
        rouletteRepository.deleteAll()
        budgetRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun `같은 유저가 동시에 2번 룰렛 요청을 보내면 정확히 1번만 성공한다`() {
        // given
        val threadCount = 2
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)

        // 충분한 예산 설정
        budgetRepository.save(DailyBudget(budgetDate = today, totalBudget = 100_000))

        // when — 동시에 룰렛 요청
        repeat(threadCount) {
            executor.submit {
                try {
                    latch.countDown()
                    latch.await() // 모든 스레드가 준비될 때까지 대기
                    rouletteService.spin(testMember.id)
                    successCount.incrementAndGet()
                } catch (e: BusinessException) {
                    if (e.errorCode == ErrorCode.ROULETTE_ALREADY_PLAYED) {
                        failCount.incrementAndGet()
                    } else {
                        throw e
                    }
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(100)
        }

        // then
        assertEquals(1, successCount.get(), "정확히 1번만 성공해야 함")
        assertEquals(1, failCount.get(), "정확히 1번만 중복 참여 에러가 발생해야 함")

        // DB 검증
        val histories = rouletteRepository.findAllByOrderByPlayedAtDesc()
        val todayHistories =
            histories.filter {
                it.memberId == testMember.id && it.playedAt == today && !it.isCancelled
            }
        assertEquals(1, todayHistories.size, "DB에 정확히 1개의 참여 기록만 있어야 함")
    }

    @Test
    fun `일일 예산을 작게 설정하고 여러 유저가 동시에 룰렛을 돌리면 당첨 포인트 합계가 예산을 초과하지 않는다`() {
        // given
        val smallBudget = 1000
        val userCount = 10
        val executor = Executors.newFixedThreadPool(userCount)
        val latch = CountDownLatch(userCount)
        val successCount = AtomicInteger(0)
        val budgetExceededCount = AtomicInteger(0)

        // 작은 예산 설정
        budgetRepository.save(DailyBudget(budgetDate = today, totalBudget = smallBudget))

        // 여러 유저 생성
        val members =
            (1..userCount).map {
                memberRepository.save(
                    Member(nickname = "budget_test_user_${System.nanoTime()}_$it", role = MemberRole.USER),
                )
            }

        // when — 동시에 룰렛 요청
        members.forEach { member ->
            executor.submit {
                try {
                    latch.countDown()
                    latch.await() // 모든 스레드가 준비될 때까지 대기
                    rouletteService.spin(member.id)
                    successCount.incrementAndGet()
                } catch (e: BusinessException) {
                    if (e.errorCode == ErrorCode.BUDGET_EXCEEDED) {
                        budgetExceededCount.incrementAndGet()
                    } else {
                        throw e
                    }
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(100)
        }

        // then
        assertTrue(successCount.get() > 0, "최소 1명은 성공해야 함")
        assertTrue(budgetExceededCount.get() > 0, "최소 1명은 예산 초과 에러가 발생해야 함")
        assertEquals(userCount, successCount.get() + budgetExceededCount.get(), "모든 요청이 처리되어야 함")

        // 예산 검증
        val budget = budgetRepository.findByBudgetDate(today).orElseThrow()
        assertTrue(budget.usedBudget <= budget.totalBudget, "사용 예산이 총 예산을 초과하면 안 됨")
        assertEquals(smallBudget, budget.totalBudget, "총 예산은 변경되지 않아야 함")

        // 실제 지급된 포인트 합계 검증
        val todayHistories =
            rouletteRepository
                .findAllByOrderByPlayedAtDesc()
                .filter { it.playedAt == today && !it.isCancelled }
        val totalGrantedPoints = todayHistories.sumOf { it.point }
        assertEquals(budget.usedBudget, totalGrantedPoints, "사용 예산과 실제 지급 포인트 합계가 일치해야 함")
        assertTrue(totalGrantedPoints <= smallBudget, "실제 지급 포인트 합계가 예산을 초과하면 안 됨")

        println(
            "테스트 결과: 성공=${successCount.get()}, 예산초과=${budgetExceededCount.get()}, 사용예산=${budget.usedBudget}/${budget.totalBudget}",
        )
    }
}
