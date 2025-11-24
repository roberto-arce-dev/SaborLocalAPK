package com.example.miappmodular.data.remote.dto.common

/**
 * Wrapper para errores del API
 */
data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val error: String? = null
)
