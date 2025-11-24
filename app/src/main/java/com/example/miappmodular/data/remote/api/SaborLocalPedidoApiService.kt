package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.data.remote.dto.pedido.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de Pedidos de SaborLocal
 */
interface SaborLocalPedidoApiService {

    /**
     * Obtiene la lista de todos los pedidos
     * GET /api/pedido
     */
    @GET("pedido")
    suspend fun getPedidos(): Response<ApiResponse<List<PedidoDto>>>

    /**
     * Obtiene un pedido específico por ID
     * GET /api/pedido/{id}
     */
    @GET("pedido/{id}")
    suspend fun getPedido(
        @Path("id") id: String
    ): Response<ApiResponse<PedidoDto>>

    /**
     * Crea un nuevo pedido
     * POST /api/pedido
     */
    @POST("pedido")
    suspend fun createPedido(
        @Body request: CreatePedidoRequest
    ): Response<ApiResponse<PedidoDto>>

    /**
     * Actualiza un pedido existente
     * PATCH /api/pedido/{id}
     */
    @PATCH("pedido/{id}")
    suspend fun updatePedido(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Response<ApiResponse<PedidoDto>>

    /**
     * Elimina un pedido
     * DELETE /api/pedido/{id}
     */
    @DELETE("pedido/{id}")
    suspend fun deletePedido(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
