package com.example.miappmodular.data.remote.dto.cliente

import com.google.gson.annotations.SerializedName

/**
 * DTO para Cliente
 * Representa un cliente que realiza pedidos.
 */
data class ClienteDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
