package com.example.roulette.order

import com.example.roulette.order.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {

    fun findByMemberIdOrderByOrderedAtDesc(memberId: Long): List<Order>

    /** 전체 주문 내역 조회 (어드민) */
    fun findAllByOrderByOrderedAtDesc(): List<Order>
}
