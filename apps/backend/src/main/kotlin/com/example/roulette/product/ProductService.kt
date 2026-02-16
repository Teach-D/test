package com.example.roulette.product

import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.order.OrderRepository
import com.example.roulette.product.dto.ProductCreateRequest
import com.example.roulette.product.dto.ProductResponse
import com.example.roulette.product.dto.ProductUpdateRequest
import com.example.roulette.product.entity.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val orderRepository: OrderRepository,
) {
    /** 활성 상품 목록 (사용자용) */
    @Transactional(readOnly = true)
    fun getActiveProducts(): List<ProductResponse> =
        productRepository
            .findByIsActiveTrueOrderByCreatedAtDesc()
            .map { toResponse(it) }

    /** 전체 상품 목록 (어드민용) */
    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductResponse> =
        productRepository
            .findAllByOrderByCreatedAtDesc()
            .map { toResponse(it) }

    /** 상품 등록 (어드민) */
    @Transactional
    fun create(request: ProductCreateRequest): ProductResponse {
        val product =
            Product(
                name = request.name,
                description = request.description,
                price = request.price,
                stock = request.stock,
            )
        return toResponse(productRepository.save(product))
    }

    /** 상품 수정 (어드민) */
    @Transactional
    fun update(
        productId: Long,
        request: ProductUpdateRequest,
    ): ProductResponse {
        val product =
            productRepository
                .findById(productId)
                .orElseThrow { BusinessException(ErrorCode.PRODUCT_NOT_FOUND) }

        request.name?.let { product.name = it }
        request.description?.let { product.description = it }
        request.price?.let { product.price = it }
        request.stock?.let { product.stock = it }
        request.isActive?.let { product.isActive = it }

        return toResponse(product)
    }

    /** 상품 삭제 (어드민) — 주문 내역이 있으면 삭제 불가 */
    @Transactional
    fun delete(productId: Long) {
        val product =
            productRepository
                .findById(productId)
                .orElseThrow { BusinessException(ErrorCode.PRODUCT_NOT_FOUND) }

        if (orderRepository.existsByProductId(product.id)) {
            throw BusinessException(ErrorCode.PRODUCT_HAS_ORDERS)
        }

        productRepository.delete(product)
    }

    /** 상품 단건 조회 */
    @Transactional(readOnly = true)
    fun getProduct(productId: Long): Product =
        productRepository
            .findById(productId)
            .orElseThrow { BusinessException(ErrorCode.PRODUCT_NOT_FOUND) }

    private fun toResponse(product: Product) =
        ProductResponse(
            id = product.id,
            name = product.name,
            description = product.description,
            price = product.price,
            stock = product.stock,
            isActive = product.isActive,
        )
}
