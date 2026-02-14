package com.example.roulette.auth.dto

data class LoginResponse(
    val accessToken: String,
    val memberId: Long,
    val nickname: String,
    val role: String,
)
