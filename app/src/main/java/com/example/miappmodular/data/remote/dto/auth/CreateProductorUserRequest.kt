package com.example.miappmodular.data.remote.dto.auth

/**
 * Request para crear PRODUCTOR en SaborLocal (solo ADMIN)
 * Usa el endpoint POST /api/auth/create-productor
 */
data class CreateProductorUserRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val ubicacion: String,
    val telefono: String
)
