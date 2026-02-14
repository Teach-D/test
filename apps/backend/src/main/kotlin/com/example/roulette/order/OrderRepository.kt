package com.example.roulette.order

import com.example.roulette.order.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {

    fun findByMemberIdOrderByOrderedAtDesc(memberId: Long): List<Order>
}
