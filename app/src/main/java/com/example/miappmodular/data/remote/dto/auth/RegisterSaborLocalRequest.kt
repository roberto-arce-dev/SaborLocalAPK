package com.example.miappmodular.data.remote.dto.auth

/**
 * Request para registro de usuario en SaborLocal
 *
 * Permite auto-registro de CLIENTE y PRODUCTOR.
 * El rol determina los campos requeridos:
 *
 * **Campos obligatorios para todos:**
 * - email, password, role, nombre
 *
 * **Campos opcionales para CLIENTE:**
 * - telefono, direccion
 *
 * **Campos para PRODUCTOR:**
 * - nombreNegocio (OBLIGATORIO)
 * - descripcion (opcional)
 * - telefono, direccion (opcionales)
 */
data class RegisterSaborLocalRequest(
    val email: String,
    val password: String,
    val role: String,  // CLIENTE o PRODUCTOR
    val nombre: String,
    val telefono: String? = null,
    val direccion: String? = null,
    val nombreNegocio: String? = null,  // Obligatorio para PRODUCTOR
    val descripcion: String? = null      // Opcional para PRODUCTOR
)
