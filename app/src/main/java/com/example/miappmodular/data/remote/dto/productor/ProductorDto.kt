package com.example.miappmodular.data.remote.dto.productor

import com.google.gson.annotations.SerializedName

/**
 * DTO para Productor
 * Representa un agricultor/productor que vende productos.
 */
data class ProductorDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
