package com.example.roulette.roulette

import com.example.roulette.common.dto.ApiResponse
import com.example.roulette.roulette.dto.RouletteHistoryResponse
import com.example.roulette.roulette.dto.RouletteResultResponse
import com.example.roulette.roulette.dto.RouletteStatusResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Tag(name = "룰렛", description = "룰렛 참여 API")
@RestController
class RouletteController(
    private val rouletteService: RouletteService,
) {
    @Operation(summary = "룰렛 돌리기", description = "오늘의 룰렛을 돌려 포인트를 획득합니다. (1일 1회)")
    @PostMapping("/api/roulette/spin")
    fun spin(authentication: Authentication): ResponseEntity<ApiResponse<RouletteResultResponse>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(rouletteService.spin(memberId)))
    }

    @Operation(summary = "룰렛 참여 상태 확인", description = "오늘 참여 여부와 잔여 예산을 확인합니다.")
    @GetMapping("/api/roulette/status")
    fun getStatus(authentication: Authentication): ResponseEntity<ApiResponse<RouletteStatusResponse>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(rouletteService.getStatus(memberId)))
    }

    @Operation(summary = "전체 룰렛 참여 내역 조회 (어드민)", description = "전체 룰렛 참여 내역을 조회합니다.")
    @GetMapping("/api/admin/roulette/histories")
    fun getAllHistories(): ResponseEntity<ApiResponse<List<RouletteHistoryResponse>>> =
        ResponseEntity.ok(ApiResponse.success(rouletteService.getAllHistories()))

    @Operation(summary = "룰렛 참여 취소 (어드민)", description = "룰렛 참여를 취소하고 포인트를 회수합니다.")
    @PostMapping("/api/admin/roulette/{historyId}/cancel")
    fun cancel(
        @PathVariable historyId: Long,
    ): ResponseEntity<ApiResponse<String>> {
        rouletteService.cancel(historyId)
        return ResponseEntity.ok(ApiResponse.success("룰렛 참여가 취소되었습니다."))
    }
}
