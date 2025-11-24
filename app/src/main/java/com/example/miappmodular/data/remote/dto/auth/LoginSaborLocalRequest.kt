package com.example.miappmodular.data.remote.dto.auth

/**
 * Request para login en SaborLocal
 */
data class LoginSaborLocalRequest(
    val email: String,
    val password: String
)
