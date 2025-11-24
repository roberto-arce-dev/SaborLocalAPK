package com.example.miappmodular.data.remote.dto.pedido

import com.google.gson.annotations.SerializedName

/**
 * DTO para Pedido
 * Representa un pedido realizado por un cliente.
 */
data class PedidoDto(
    @SerializedName("_id")
    val id: String,
    val cliente: Any,  // Puede ser String (ID) o ClienteDto (populated)
    val items: List<PedidoItemDto>,
    val total: Double,
    val estado: String,  // pendiente, en_preparacion, en_camino, entregado, cancelado
    val fecha: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
