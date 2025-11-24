package com.example.miappmodular.data.remote.dto.entrega

import com.google.gson.annotations.SerializedName

/**
 * DTO para Entrega
 * Representa la información de entrega de un pedido.
 */
data class EntregaDto(
    @SerializedName("_id")
    val id: String,
    val pedido: Any,  // Puede ser String (ID) o PedidoDto (populated)
    val direccion: String,
    val fechaEntrega: String,
    val estado: String,  // pendiente, en_camino, entregado
    val repartidor: String,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
