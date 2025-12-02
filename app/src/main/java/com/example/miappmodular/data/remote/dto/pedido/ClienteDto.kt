package com.example.miappmodular.data.remote.dto.pedido

import com.google.gson.annotations.SerializedName

/**
 * DTO para Cliente cuando viene poblado en Pedido
 *
 * El backend ahora retorna el cliente como objeto completo:
 * ```json
 * "cliente": {
 *   "_id": "69263fd8a744f4759b9937a9",
 *   "nombre": "Roberto Arce"
 * }
 * ```
 */
data class ClienteDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String? = null,  // Nombre del cliente (puede ser null)
    val email: String? = null,  // Email (opcional si no lo incluye el backend)
    val telefono: String? = null,  // Teléfono (opcional)
    val direccion: String? = null  // Dirección (opcional)
)
