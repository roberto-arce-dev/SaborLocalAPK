package com.example.miappmodular.data.remote.dto.common

/**
 * Wrapper genérico para respuestas exitosas del API
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val total: Int? = null
)
