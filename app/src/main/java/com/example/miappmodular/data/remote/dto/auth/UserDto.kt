package com.example.miappmodular.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * DTO para Usuario (User)
 * Representa un usuario del sistema (CLIENTE, PRODUCTOR o ADMIN)
 *
 * **IMPORTANTE:** El campo `nombre` puede ser null en el API.
 * Algunos usuarios creados directamente sin el campo `nombre` no lo tienen.
 */
data class UserDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String? = null,  // ✅ Nullable - algunos usuarios no lo tienen
    val email: String,
    val role: String,  // CLIENTE, PRODUCTOR, ADMIN
    val telefono: String? = null,
    val direccion: String? = null,
    val ubicacion: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)


