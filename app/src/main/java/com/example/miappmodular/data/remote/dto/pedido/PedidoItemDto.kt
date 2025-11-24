package com.example.miappmodular.data.remote.dto.pedido

import com.example.miappmodular.data.remote.dto.producto.ProductoDto

/**
 * Item de producto en un pedido
 */
data class PedidoItemDto(
    val producto: Any,  // Puede ser String (ID) o ProductoDto (populated)
    val cantidad: Int,
    val precio: Double
) {
    fun getProductoPopulated(): ProductoDto? {
        return when (producto) {
            is Map<*, *> -> {
                val map = producto as Map<String, Any>
                ProductoDto(
                    id = map["_id"] as? String ?: "",
                    nombre = map["nombre"] as? String ?: "",
                    descripcion = map["descripcion"] as? String ?: "",
                    precio = (map["precio"] as? Number)?.toDouble() ?: 0.0,
                    unidad = map["unidad"] as? String ?: "",
                    stock = (map["stock"] as? Number)?.toInt() ?: 0,
                    productor = map["productor"] ?: "",
                    imagen = map["imagen"] as? String,
                    imagenThumbnail = map["imagenThumbnail"] as? String
                )
            }
            else -> null
        }
    }
}
