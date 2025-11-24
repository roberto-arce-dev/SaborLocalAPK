package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.data.remote.dto.entrega.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de Entregas de SaborLocal
 */
interface SaborLocalEntregaApiService {

    /**
     * Obtiene la lista de todas las entregas
     * GET /api/entrega
     */
    @GET("entrega")
    suspend fun getEntregas(): Response<ApiResponse<List<EntregaDto>>>

    /**
     * Obtiene una entrega específica por ID
     * GET /api/entrega/{id}
     */
    @GET("entrega/{id}")
    suspend fun getEntrega(
        @Path("id") id: String
    ): Response<ApiResponse<EntregaDto>>

    /**
     * Crea una nueva entrega
     * POST /api/entrega
     */
    @POST("entrega")
    suspend fun createEntrega(
        @Body request: Map<String, Any>
    ): Response<ApiResponse<EntregaDto>>

    /**
     * Actualiza una entrega existente
     * PATCH /api/entrega/{id}
     */
    @PATCH("entrega/{id}")
    suspend fun updateEntrega(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Response<ApiResponse<EntregaDto>>

    /**
     * Elimina una entrega
     * DELETE /api/entrega/{id}
     */
    @DELETE("entrega/{id}")
    suspend fun deleteEntrega(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
