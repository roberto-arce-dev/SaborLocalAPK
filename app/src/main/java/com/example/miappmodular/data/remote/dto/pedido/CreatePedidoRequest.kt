package com.example.miappmodular.data.remote.dto.pedido

/**
 * Request para crear un nuevo Pedido
 */
data class CreatePedidoRequest(
    val cliente: String,  // ID del cliente
    val items: List<PedidoItemRequest>,
    val total: Double
)
