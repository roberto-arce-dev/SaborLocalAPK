package com.example.miappmodular.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * DTO para Usuario (User)
 * Representa un usuario del sistema (CLIENTE, PRODUCTOR o ADMIN)
 */
data class UserDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val email: String,
    val role: String,  // CLIENTE, PRODUCTOR, ADMIN
    val telefono: String? = null,
    val direccion: String? = null,
    val ubicacion: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
