package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.data.remote.dto.cliente.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de Clientes de SaborLocal
 */
interface SaborLocalClienteApiService {

    /**
     * Obtiene la lista de todos los clientes
     * GET /api/cliente
     */
    @GET("cliente")
    suspend fun getClientes(): Response<ApiResponse<List<ClienteDto>>>

    /**
     * Obtiene un cliente específico por ID
     * GET /api/cliente/{id}
     */
    @GET("cliente/{id}")
    suspend fun getCliente(
        @Path("id") id: String
    ): Response<ApiResponse<ClienteDto>>

    /**
     * Crea un nuevo cliente
     * POST /api/cliente
     */
    @POST("cliente")
    suspend fun createCliente(
        @Body request: CreateClienteRequest
    ): Response<ApiResponse<ClienteDto>>

    /**
     * Actualiza un cliente existente
     * PATCH /api/cliente/{id}
     */
    @PATCH("cliente/{id}")
    suspend fun updateCliente(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Response<ApiResponse<ClienteDto>>

    /**
     * Elimina un cliente
     * DELETE /api/cliente/{id}
     */
    @DELETE("cliente/{id}")
    suspend fun deleteCliente(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
