package com.example.roulette.product

import com.example.roulette.common.dto.ApiResponse
import com.example.roulette.product.dto.ProductCreateRequest
import com.example.roulette.product.dto.ProductResponse
import com.example.roulette.product.dto.ProductUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "상품", description = "상품 관리 API")
@RestController
class ProductController(
    private val productService: ProductService,
) {
    @Operation(summary = "상품 목록 조회 (사용자)", description = "판매 중인 상품 목록을 조회합니다.")
    @GetMapping("/api/products")
    fun getActiveProducts(): ResponseEntity<ApiResponse<List<ProductResponse>>> =
        ResponseEntity.ok(ApiResponse.success(productService.getActiveProducts()))

    @Operation(summary = "전체 상품 목록 조회 (어드민)", description = "모든 상품을 조회합니다.")
    @GetMapping("/api/admin/products")
    fun getAllProducts(): ResponseEntity<ApiResponse<List<ProductResponse>>> =
        ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()))

    @Operation(summary = "상품 등록 (어드민)", description = "새 상품을 등록합니다.")
    @PostMapping("/api/admin/products")
    fun create(
        @Valid @RequestBody request: ProductCreateRequest,
    ): ResponseEntity<ApiResponse<ProductResponse>> =
        ResponseEntity.ok(ApiResponse.success(productService.create(request)))

    @Operation(summary = "상품 수정 (어드민)", description = "상품 정보를 수정합니다.")
    @PatchMapping("/api/admin/products/{productId}")
    fun update(
        @PathVariable productId: Long,
        @Valid @RequestBody request: ProductUpdateRequest,
    ): ResponseEntity<ApiResponse<ProductResponse>> =
        ResponseEntity.ok(ApiResponse.success(productService.update(productId, request)))
}
