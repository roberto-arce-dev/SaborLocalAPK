package com.example.miappmodular.data.remote.dto.productor

/**
 * Request para crear un nuevo Productor
 */
data class CreateProductorRequest(
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String
)
