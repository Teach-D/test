package com.example.roulette.order

import com.example.roulette.order.entity.PointUsage
import org.springframework.data.jpa.repository.JpaRepository

interface PointUsageRepository : JpaRepository<PointUsage, Long> {

    fun findByOrderId(orderId: Long): List<PointUsage>
}
