package com.example.miappmodular.data.remote.dto.auth

/**
 * Request para registro de CLIENTE en SaborLocal
 * Solo CLIENTES pueden auto-registrarse
 */
data class RegisterSaborLocalRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null
)
