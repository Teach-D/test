package com.example.roulette.point

import com.example.roulette.common.dto.ApiResponse
import com.example.roulette.point.dto.PointBalanceResponse
import com.example.roulette.point.dto.PointResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "포인트", description = "포인트 조회 API")
@RestController
@RequestMapping("/api/points")
class PointController(
    private val pointService: PointService,
) {
    @Operation(summary = "포인트 잔액 조회", description = "내 포인트 잔액과 만료 예정 포인트를 조회합니다.")
    @GetMapping("/balance")
    fun getBalance(authentication: Authentication): ResponseEntity<ApiResponse<PointBalanceResponse>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(pointService.getBalance(memberId)))
    }

    @Operation(summary = "포인트 내역 조회", description = "내 포인트 적립/사용 내역을 조회합니다.")
    @GetMapping
    fun getPoints(authentication: Authentication): ResponseEntity<ApiResponse<List<PointResponse>>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(pointService.getPoints(memberId)))
    }

    @Operation(summary = "만료 예정 포인트 조회", description = "7일 이내 만료 예정인 포인트를 조회합니다.")
    @GetMapping("/expiring-soon")
    fun getExpiringSoon(authentication: Authentication): ResponseEntity<ApiResponse<List<PointResponse>>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(pointService.getExpiringSoon(memberId)))
    }
}
