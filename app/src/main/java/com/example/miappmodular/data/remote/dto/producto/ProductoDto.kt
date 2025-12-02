package com.example.miappmodular.data.remote.dto.producto

import com.example.miappmodular.data.remote.dto.productor.ProductorDto
import com.google.gson.annotations.SerializedName

/**
 * DTO para Producto
 * Representa un producto agrícola que se vende en la plataforma.
 *
 * **Estructura variable según el endpoint:**
 * - `/producto` → `productor` es objeto parcial: `{_id, telefono}`
 * - `/producto/productor/{id}` → `productorId` es String (solo ID)
 *
 * **IMPORTANTE:** El campo `productor` puede ser:
 * - String (solo ID)
 * - Map con {_id, telefono} (objeto parcial)
 * - Map completo con todos los campos (objeto populado)
 *
 * Usamos `Any` para manejar todos los casos.
 */
data class ProductoDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,  // kg, unidad, litro, etc.
    val categoria: String? = null,  // verduras, frutas, lacteos, etc.
    val productor: Any,  // Puede ser String (ID) o Map (objeto parcial/completo)
    val disponible: Boolean = true,
    val stock: Int,
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Obtiene el ID del productor, sin importar el formato
     */
    fun getProductorId(): String {
        return when (productor) {
            is String -> productor
            is Map<*, *> -> (productor as Map<String, Any>)["_id"] as? String ?: ""
            else -> ""
        }
    }
}
