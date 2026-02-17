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
    var role: MemberRole = MemberRole.USER,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
) : BaseTimeEntity() {
    /** 회원 역할을 변경한다 */
    fun changeRole(newRole: MemberRole) {
        role = newRole
    }
}

enum class MemberRole {
    USER,
    ADMIN,
}
