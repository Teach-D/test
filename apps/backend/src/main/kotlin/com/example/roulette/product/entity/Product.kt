package com.example.roulette.product.entity

import com.example.roulette.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var price: Int,

    @Column(nullable = false)
    var stock: Int = 0,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity() {

    /** 재고 차감 */
    fun decreaseStock() {
        stock -= 1
    }

    /** 재고 복원 */
    fun increaseStock() {
        stock += 1
    }
}
