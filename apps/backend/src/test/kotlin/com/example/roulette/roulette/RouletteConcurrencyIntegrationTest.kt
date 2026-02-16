package com.example.roulette.roulette

import com.example.roulette.auth.MemberRepository
import com.example.roulette.auth.entity.Member
import com.example.roulette.auth.entity.MemberRole
import com.example.roulette.budget.BudgetRepository
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

    @BeforeEach
    fun setUp() {
        testMember =
            memberRepository.save(
                Member(nickname = "concurrency_test_user_${System.nanoTime()}", role = MemberRole.USER),
            )
    }

    @AfterEach
    fun tearDown() {
        pointRepository.deleteAll()
        rouletteRepository.deleteAll()
        budgetRepository.deleteAll()
        memberRepository.deleteAll()
    }

    @Test
    fun `같은 유저가 하루에 두 번 룰렛을 돌리면 두 번째는 실패한다`() {
        // given — 예산은 서비스가 자동 생성 (DEFAULT_BUDGET)

        // when — 첫 번째 참여: 성공
        val result = rouletteService.spin(testMember.id)
        assertTrue(result.point in 100..1000, "첫 번째 참여는 성공해야 함")

        // then — 두 번째 참여: ROULETTE_ALREADY_PLAYED 예외
        val exception =
            assertThrows(BusinessException::class.java) {
                rouletteService.spin(testMember.id)
            }
        assertEquals(ErrorCode.ROULETTE_ALREADY_PLAYED, exception.errorCode)

        // DB 검증 — 해당 멤버의 참여 기록이 정확히 1개
        val memberHistories =
            rouletteRepository
                .findAllByOrderByPlayedAtDesc()
                .filter { it.memberId == testMember.id && !it.isCancelled }
        assertEquals(1, memberHistories.size, "DB에 정확히 1개의 참여 기록만 있어야 함")
    }

    /**
     * 예산 동시성 테스트(비관적 락)는 H2에서 FOR UPDATE가 멀티스레드 환경을
     * 제대로 직렬화하지 못하므로 k6 + PostgreSQL에서 검증합니다.
     * @see apps/backend/k6/budget-load-test.js
     */
}
