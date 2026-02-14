package com.example.roulette.auth.entity

import com.example.roulette.common.entity.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "members")
class Member(
    @Column(nullable = false, unique = true, length = 50)
    val nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val role: MemberRole = MemberRole.USER,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity()

enum class MemberRole {
    USER, ADMIN
}
