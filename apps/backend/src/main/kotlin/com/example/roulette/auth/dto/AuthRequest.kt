package com.example.roulette.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class LoginRequest(
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 2, max = 50, message = "닉네임은 2~50자 사이여야 합니다.")
    val nickname: String,
)
