package com.example.roulette.budget

import com.example.roulette.budget.dto.BudgetResponse
import com.example.roulette.budget.dto.BudgetSetRequest
import com.example.roulette.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "예산", description = "일일 예산 관리 API")
@RestController
class BudgetController(
    private val budgetService: BudgetService,
) {
    @Operation(summary = "오늘 예산 조회", description = "오늘의 일일 예산 현황을 조회합니다.")
    @GetMapping("/api/budget/today")
    fun getTodayBudget(): ResponseEntity<ApiResponse<BudgetResponse>> =
        ResponseEntity.ok(ApiResponse.success(budgetService.getTodayBudget()))

    @Operation(summary = "오늘 예산 설정 (어드민)", description = "오늘의 일일 예산을 설정합니다.")
    @PutMapping("/api/admin/budget/today")
    fun setTodayBudget(
        @Valid @RequestBody request: BudgetSetRequest,
    ): ResponseEntity<ApiResponse<BudgetResponse>> =
        ResponseEntity.ok(ApiResponse.success(budgetService.setTodayBudget(request)))
}
