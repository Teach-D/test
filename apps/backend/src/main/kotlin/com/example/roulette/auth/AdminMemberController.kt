package com.example.roulette.auth

import com.example.roulette.auth.dto.MemberResponse
import com.example.roulette.common.dto.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "관리자 - 회원 관리", description = "관리자 계정 관리 API")
@RestController
@RequestMapping("/api/admin/members")
class AdminMemberController(
    private val authService: AuthService,
) {
    @Operation(
        summary = "관리자 계정 생성/승격",
        description = "닉네임으로 ADMIN 계정을 생성하거나 기존 회원을 ADMIN으로 승격합니다.",
    )
    @PostMapping
    fun createAdmin(
        @RequestBody request: CreateAdminRequest,
    ): ResponseEntity<ApiResponse<MemberResponse>> {
        val response = authService.createAdmin(request.nickname)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))
    }

    @Operation(summary = "관리자 목록 조회", description = "ADMIN 역할을 가진 회원 목록을 반환합니다.")
    @GetMapping
    fun getAdminMembers(): ResponseEntity<ApiResponse<List<MemberResponse>>> {
        val members = authService.getAdminMembers()
        return ResponseEntity.ok(ApiResponse.success(members))
    }

    @Operation(
        summary = "관리자 권한 제거",
        description = "해당 회원의 역할을 USER로 변경합니다. 자기 자신의 권한은 제거할 수 없습니다.",
    )
    @DeleteMapping("/{id}")
    fun demoteAdmin(
        @PathVariable id: Long,
        @AuthenticationPrincipal requesterId: Long,
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.demoteAdmin(adminId = id, requesterId = requesterId)
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }
}

/** 관리자 생성 요청 DTO */
data class CreateAdminRequest(
    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 2, max = 50, message = "닉네임은 2~50자 사이여야 합니다.")
    val nickname: String,
)