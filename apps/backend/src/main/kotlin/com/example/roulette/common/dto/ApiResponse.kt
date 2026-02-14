package com.example.roulette.common.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val status: String,
    val data: T? = null,
    val errorCode: String? = null,
    val message: String? = null,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(status = "SUCCESS", data = data)

        fun error(errorCode: String, message: String): ApiResponse<Nothing> =
            ApiResponse(status = "ERROR", errorCode = errorCode, message = message)
    }
}
