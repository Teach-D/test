package com.example.roulette.order

import com.example.roulette.common.exception.BusinessException
import com.example.roulette.common.exception.ErrorCode
import com.example.roulette.order.dto.OrderRequest
import com.example.roulette.order.dto.OrderResponse
import com.example.roulette.order.entity.Order
import com.example.roulette.order.entity.PointUsage
import com.example.roulette.point.PointService
import com.example.roulette.product.ProductService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val pointUsageRepository: PointUsageRepository,
    private val productService: ProductService,
    private val pointService: PointService,
) {

    /** 상품 주문 (포인트 차감) */
    @Transactional
    fun placeOrder(memberId: Long, request: OrderRequest): OrderResponse {
        val product = productService.getProduct(request.productId)

        // 상품 유효성 체크
        if (!product.isActive) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_ACTIVE)
        }
        if (product.stock <= 0) {
            throw BusinessException(ErrorCode.PRODUCT_OUT_OF_STOCK)
        }

        // 포인트 차감 (FIFO)
        val usages = pointService.deduct(memberId, product.price)

        // 재고 차감
        product.decreaseStock()

        // 주문 생성
        val order = orderRepository.save(
            Order(
                memberId = memberId,
                productId = product.id,
                usedPoint = product.price,
            )
        )

        // 포인트 사용 내역 저장
        usages.forEach { (pointId, amount) ->
            pointUsageRepository.save(PointUsage(pointId = pointId, orderId = order.id, amount = amount))
        }

        return toResponse(order, product.name)
    }

    /** 주문 취소 (어드민 — 포인트 환불) */
    @Transactional
    fun cancelOrder(orderId: Long): OrderResponse {
        val order = orderRepository.findById(orderId)
            .orElseThrow { BusinessException(ErrorCode.ORDER_NOT_FOUND) }

        if (order.status == com.example.roulette.order.entity.OrderStatus.CANCELLED) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }

        // 1. 주문 상태 변경
        order.cancel()

        // 2. 포인트 환불
        val usages = pointUsageRepository.findByOrderId(orderId)
            .map { it.pointId to it.amount }
        pointService.restore(usages)

        // 3. 재고 복원
        val product = productService.getProduct(order.productId)
        product.increaseStock()

        return toResponse(order, product.name)
    }

    /** 내 주문 내역 조회 */
    @Transactional(readOnly = true)
    fun getMyOrders(memberId: Long): List<OrderResponse> {
        return orderRepository.findByMemberIdOrderByOrderedAtDesc(memberId)
            .map { order ->
                val product = productService.getProduct(order.productId)
                toResponse(order, product.name)
            }
    }

    /** 전체 주문 목록 조회 (어드민) */
    @Transactional(readOnly = true)
    fun getAllOrders(): List<OrderResponse> {
        return orderRepository.findAllByOrderByOrderedAtDesc()
            .map { order ->
                val product = productService.getProduct(order.productId)
                toResponse(order, product.name)
            }
    }

    private fun toResponse(order: Order, productName: String) = OrderResponse(
        id = order.id,
        productId = order.productId,
        productName = productName,
        usedPoint = order.usedPoint,
        status = order.status.name,
        orderedAt = order.orderedAt,
    )
}
