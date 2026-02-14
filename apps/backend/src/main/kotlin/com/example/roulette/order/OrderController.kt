package com.example.roulette.order

import com.example.roulette.common.dto.ApiResponse
import com.example.roulette.order.dto.OrderRequest
import com.example.roulette.order.dto.OrderResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@Tag(name = "주문", description = "상품 주문 API")
@RestController
class OrderController(
    private val orderService: OrderService,
) {

    @Operation(summary = "상품 주문", description = "포인트를 사용하여 상품을 주문합니다.")
    @PostMapping("/api/orders")
    fun placeOrder(
        authentication: Authentication,
        @RequestBody request: OrderRequest,
    ): ResponseEntity<ApiResponse<OrderResponse>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(orderService.placeOrder(memberId, request)))
    }

    @Operation(summary = "내 주문 내역 조회", description = "나의 주문 내역을 조회합니다.")
    @GetMapping("/api/orders")
    fun getMyOrders(authentication: Authentication): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        val memberId = authentication.principal as Long
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(memberId)))
    }

    @Operation(summary = "전체 주문 목록 조회 (어드민)", description = "전체 주문 목록을 조회합니다.")
    @GetMapping("/api/admin/orders")
    fun getAllOrders(): ResponseEntity<ApiResponse<List<OrderResponse>>> {
        return ResponseEntity.ok(ApiResponse.success(orderService.getAllOrders()))
    }

    @Operation(summary = "주문 취소 (어드민)", description = "주문을 취소하고 포인트를 환불합니다.")
    @PostMapping("/api/admin/orders/{orderId}/cancel")
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<ApiResponse<OrderResponse>> {
        return ResponseEntity.ok(ApiResponse.success(orderService.cancelOrder(orderId)))
    }
}
