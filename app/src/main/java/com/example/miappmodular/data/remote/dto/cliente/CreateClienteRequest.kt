package com.example.miappmodular.data.remote.dto.cliente

/**
 * Request para crear un nuevo Cliente
 */
data class CreateClienteRequest(
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String
)
