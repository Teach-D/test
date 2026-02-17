package com.example.roulette.auth

import com.example.roulette.auth.dto.LoginRequest
import com.example.roulette.auth.dto.LoginResponse
import com.example.roulette.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증", description = "로그인 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val jwtProvider: JwtProvider,
) {
    @Operation(summary = "로그인", description = "닉네임으로 로그인합니다. 없는 닉네임이면 자동으로 회원가입됩니다.")
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "관리자 로그인", description = "관리자 웹 전용 로그인입니다. 없는 닉네임이면 ADMIN으로 자동 회원가입됩니다.")
    @PostMapping("/admin-login")
    fun adminLogin(
        @Valid @RequestBody request: LoginRequest,
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.adminLogin(request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(summary = "인증 디버그", description = "JWT 토큰 검증 상태를 확인합니다.")
    @GetMapping("/debug")
    fun debug(request: HttpServletRequest): ResponseEntity<ApiResponse<Map<String, Any?>>> {
        val authHeader = request.getHeader("Authorization")
        val token = if (authHeader?.startsWith("Bearer ") == true) authHeader.substring(7) else null
        val result =
            mutableMapOf<String, Any?>(
                "hasAuthHeader" to (authHeader != null),
                "authHeaderPrefix" to authHeader?.take(10),
                "tokenLength" to token?.length,
                "securityContextAuth" to SecurityContextHolder.getContext().authentication?.toString(),
            )
        if (token != null) {
            try {
                val isValid = jwtProvider.isValid(token)
                result["isValid"] = isValid
                if (isValid) {
                    result["memberId"] = jwtProvider.getMemberId(token)
                    result["role"] = jwtProvider.getRole(token)
                }
            } catch (e: Exception) {
                result["validationError"] = "${e.javaClass.simpleName}: ${e.message}"
            }
        }
        return ResponseEntity.ok(ApiResponse.success(result))
    }
}
