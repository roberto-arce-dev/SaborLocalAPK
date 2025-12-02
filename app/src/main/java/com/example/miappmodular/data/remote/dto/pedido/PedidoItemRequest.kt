package com.example.miappmodular.data.remote.dto.pedido

/**
 * Item de producto en un request de pedido
 *
 * **Alineado con el backend (NestJS):**
 * El backend solo requiere el ID del producto y la cantidad.
 * El precio se obtiene desde la base de datos para evitar manipulación.
 */
data class PedidoItemRequest(
    val producto: String,  // ID del producto
    val cantidad: Int
)
