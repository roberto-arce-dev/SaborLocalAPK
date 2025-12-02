package com.example.miappmodular.data.remote.dto.pedido

import com.google.gson.annotations.SerializedName

/**
 * DTO para Pedido según la respuesta REAL de la API SaborLocal
 *
 * **Estructura real del backend (actualizada):**
 * ```json
 * {
 *   "_id": "6928acea219c3067b0dbb904",
 *   "cliente": {
 *     "_id": "69263fd8a744f4759b9937a9",
 *     "nombre": "Roberto Arce"
 *   },
 *   "items": [{"producto":"...", "cantidad":1, "precio":1500}],
 *   "total": 1500,
 *   "estado": "pendiente",
 *   "createdAt": "2025-11-27T19:56:26.566Z",
 *   "updatedAt": "2025-11-27T19:56:26.566Z"
 * }
 * ```
 *
 * **Nota:** El backend ahora usa `.populate('cliente')` para retornar el objeto completo
 * en lugar de solo el ID.
 *
 * **Estados posibles:**
 * - pendiente, en_preparacion, en_camino, entregado, cancelado
 */
data class PedidoDto(
    @SerializedName("_id")
    val id: String,
    val cliente: ClienteDto,  // ✅ Backend retorna objeto completo (con .populate())
    val items: List<PedidoItemDto>,  // ✅ Backend retorna "items", no "productos"
    val total: Double,
    val estado: String,  // pendiente, en_preparacion, en_camino, entregado, cancelado
    val direccionEntrega: String? = null,  // String opcional
    val notasEntrega: String? = null,  // String opcional
    val createdAt: String? = null,  // ✅ Backend usa createdAt como fecha del pedido
    val updatedAt: String? = null
)

