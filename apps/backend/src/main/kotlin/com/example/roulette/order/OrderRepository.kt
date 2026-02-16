package com.example.roulette.order

import com.example.roulette.order.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findByMemberIdOrderByOrderedAtDesc(memberId: Long): List<Order>

    /** 전체 주문 내역 조회 (어드민) */
    fun findAllByOrderByOrderedAtDesc(): List<Order>

    /** 상품별 주문 존재 여부 확인 */
    fun existsByProductId(productId: Long): Boolean
}
