package com.example.miappmodular.data.remote.dto.pedido

/**
 * Request para crear un nuevo Pedido
 *
 * **Alineado con el backend (NestJS):**
 * - El clienteId se obtiene automáticamente del token JWT (no se envía)
 * - El total se calcula en el backend (no se envía para evitar manipulación)
 * - direccionEntrega y notasEntrega son opcionales
 */
data class CreatePedidoRequest(
    val items: List<PedidoItemRequest>,
    val direccionEntrega: String? = null,
    val notasEntrega: String? = null
)
