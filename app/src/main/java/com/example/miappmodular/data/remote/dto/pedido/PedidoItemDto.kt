package com.example.miappmodular.data.remote.dto.pedido

/**
 * Item de producto en un pedido según la respuesta REAL del backend
 *
 * **Estructura real del backend:**
 * ```json
 * {
 *   "producto": "6925bc737664a9a746935994",  // String ID
 *   "cantidad": 1,
 *   "precio": 1500
 * }
 * ```
 *
 * **Nota:** El backend SaborLocal retorna solo el ID del producto,
 * no el objeto completo. El nombre y otros datos se deben obtener
 * haciendo una petición GET /api/producto/{id} si es necesario.
 */
data class PedidoItemDto(
    val producto: String,  // ✅ Backend retorna "producto" (String ID), no "productoId"
    val cantidad: Int,
    val precio: Double
)
