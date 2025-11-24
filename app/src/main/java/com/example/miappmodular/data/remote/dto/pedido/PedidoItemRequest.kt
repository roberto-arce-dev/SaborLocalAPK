package com.example.miappmodular.data.remote.dto.pedido

/**
 * Item de producto en un request de pedido
 */
data class PedidoItemRequest(
    val producto: String,  // ID del producto
    val cantidad: Int,
    val precio: Double
)
