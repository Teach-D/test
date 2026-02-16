package com.example.roulette.product.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Int,
    val stock: Int,
    val isActive: Boolean,
)

data class ProductCreateRequest(
    @field:NotBlank(message = "상품명을 입력해주세요.")
    @field:Size(max = 100, message = "상품명은 100자 이내여야 합니다.")
    val name: String,
    @field:Size(max = 500, message = "설명은 500자 이내여야 합니다.")
    val description: String? = null,
    @field:Min(value = 1, message = "가격은 1 이상이어야 합니다.")
    val price: Int,
    @field:Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    val stock: Int = 0,
)

data class ProductUpdateRequest(
    @field:Size(max = 100, message = "상품명은 100자 이내여야 합니다.")
    val name: String? = null,
    @field:Size(max = 500, message = "설명은 500자 이내여야 합니다.")
    val description: String? = null,
    @field:Min(value = 1, message = "가격은 1 이상이어야 합니다.")
    val price: Int? = null,
    @field:Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    val stock: Int? = null,
    val isActive: Boolean? = null,
)
