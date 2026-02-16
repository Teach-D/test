package com.example.roulette.product

import com.example.roulette.product.entity.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long> {
    /** 활성 상품 목록 */
    fun findByIsActiveTrueOrderByCreatedAtDesc(): List<Product>

    /** 전체 상품 목록 (어드민용) */
    fun findAllByOrderByCreatedAtDesc(): List<Product>
}
