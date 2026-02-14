package com.example.roulette.roulette.entity

import com.example.roulette.common.entity.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "roulette_histories",
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "played_at"])]
)
class RouletteHistory(
    @Column(nullable = false)
    val memberId: Long,

    @Column(nullable = false)
    val point: Int,

    @Column(nullable = false)
    var isCancelled: Boolean = false,

    @Column(nullable = false)
    val playedAt: LocalDate = LocalDate.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity() {

    fun cancel() {
        isCancelled = true
    }
}
