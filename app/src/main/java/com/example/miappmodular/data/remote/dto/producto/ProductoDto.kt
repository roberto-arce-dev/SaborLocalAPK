package com.example.miappmodular.data.remote.dto.producto

import com.example.miappmodular.data.remote.dto.productor.ProductorDto
import com.google.gson.annotations.SerializedName

/**
 * DTO para Producto
 * Representa un producto agrícola que se vende en la plataforma.
 * Puede incluir el productor completo (populate) o solo el ID.
 */
data class ProductoDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,  // kg, unidad, litro, etc.
    val stock: Int,
    val productor: Any,  // Puede ser String (ID) o ProductorDto (populated)
    val imagen: String? = null,
    val imagenThumbnail: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    /**
     * Helper para obtener el productor si fue populated
     */
    fun getProductorPopulated(): ProductorDto? {
        return when (productor) {
            is Map<*, *> -> {
                // Convert map to ProductorDto
                val map = productor as Map<String, Any>
                ProductorDto(
                    id = map["_id"] as? String ?: "",
                    nombre = map["nombre"] as? String ?: "",
                    ubicacion = map["ubicacion"] as? String ?: "",
                    telefono = map["telefono"] as? String ?: "",
                    email = map["email"] as? String ?: "",
                    imagen = map["imagen"] as? String,
                    imagenThumbnail = map["imagenThumbnail"] as? String
                )
            }
            else -> null
        }
    }

    /**
     * Helper para obtener el ID del productor
     */
    fun getProductorId(): String {
        return when (productor) {
            is String -> productor
            is Map<*, *> -> (productor as Map<String, Any>)["_id"] as? String ?: ""
            else -> ""
        }
    }
}
