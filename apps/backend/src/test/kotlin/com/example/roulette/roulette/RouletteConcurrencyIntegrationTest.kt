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
    fun `같은 유저가 하루에 두 번 룰렛을 돌리면 두 번째는 실패한다`() {
        // given — 충분한 예산 설정
        budgetRepository.save(DailyBudget(budgetDate = today, totalBudget = 100_000))

        // when — 첫 번째 참여: 성공
        val result = rouletteService.spin(testMember.id)
        assertTrue(result.point in 100..1000, "첫 번째 참여는 성공해야 함")

        // then — 두 번째 참여: ROULETTE_ALREADY_PLAYED 예외
        val exception =
            assertThrows(BusinessException::class.java) {
                rouletteService.spin(testMember.id)
            }
        assertEquals(ErrorCode.ROULETTE_ALREADY_PLAYED, exception.errorCode)

        // DB 검증 — 정확히 1개의 참여 기록만 존재
        val todayHistories =
            rouletteRepository
                .findAllByOrderByPlayedAtDesc()
                .filter {
                    it.memberId == testMember.id && it.playedAt == today && !it.isCancelled
                }
        assertEquals(1, todayHistories.size, "DB에 정확히 1개의 참여 기록만 있어야 함")
    }

    @Test
    fun `일일 예산을 작게 설정하고 여러 유저가 동시에 룰렛을 돌리면 당첨 포인트 합계가 예산을 초과하지 않는다`() {
        // given
        val smallBudget = 500
        val userCount = 10
        val executor = Executors.newFixedThreadPool(userCount)
        val latch = CountDownLatch(userCount)
        val successCount = AtomicInteger(0)

        // 작은 예산 설정
        budgetRepository.save(DailyBudget(budgetDate = today, totalBudget = smallBudget))

        // 여러 유저 생성
        val members =
            (1..userCount).map {
                memberRepository.save(
                    Member(
                        nickname = "budget_test_user_${System.nanoTime()}_$it",
                        role = MemberRole.USER,
                    ),
                )
            }

        // when — 동시에 룰렛 요청
        members.forEach { member ->
            executor.submit {
                try {
                    latch.countDown()
                    latch.await()
                    rouletteService.spin(member.id)
                    successCount.incrementAndGet()
                } catch (_: Exception) {
                    // 예산 초과 시 BusinessException 또는 트랜잭션 관련 예외 발생
                }
            }
        }

        executor.shutdown()
        while (!executor.isTerminated) {
            Thread.sleep(100)
        }

        // then — DB 상태 기반 검증 (핵심 불변식)
        val budget = budgetRepository.findByBudgetDate(today).orElseThrow()
        assertTrue(
            budget.usedBudget <= budget.totalBudget,
            "사용 예산(${budget.usedBudget})이 총 예산(${budget.totalBudget})을 초과하면 안 됨",
        )

        // 실제 지급된 포인트 합계 검증
        val todayHistories =
            rouletteRepository
                .findAllByOrderByPlayedAtDesc()
                .filter { it.playedAt == today && !it.isCancelled }
        val totalGrantedPoints = todayHistories.sumOf { it.point }
        assertTrue(
            totalGrantedPoints <= smallBudget,
            "실제 지급 포인트 합계($totalGrantedPoints)가 예산($smallBudget)을 초과하면 안 됨",
        )

        // 최소 1명은 성공, 전원 성공은 불가 (500/100=최대5명, 10명 시도)
        assertTrue(successCount.get() in 1..5, "성공 수(${successCount.get()})는 1~5명 이어야 함")

        println(
            "테스트 결과: 성공=${successCount.get()}, 사용예산=${budget.usedBudget}/${budget.totalBudget}",
        )
    }
}
