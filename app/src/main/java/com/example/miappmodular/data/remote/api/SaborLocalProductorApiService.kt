package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.data.remote.dto.productor.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de Productores de SaborLocal
 */
interface SaborLocalProductorApiService {

    /**
     * Obtiene la lista de todos los productores
     * GET /api/productor
     */
    @GET("productor")
    suspend fun getProductores(): Response<ApiResponse<List<ProductorDto>>>

    /**
     * Obtiene un productor específico por ID
     * GET /api/productor/{id}
     */
    @GET("productor/{id}")
    suspend fun getProductor(
        @Path("id") id: String
    ): Response<ApiResponse<ProductorDto>>

    /**
     * Crea un nuevo productor
     * POST /api/productor
     */
    @POST("productor")
    suspend fun createProductor(
        @Body request: CreateProductorRequest
    ): Response<ApiResponse<ProductorDto>>

    /**
     * Actualiza un productor existente
     * PATCH /api/productor/{id}
     */
    @PATCH("productor/{id}")
    suspend fun updateProductor(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Response<ApiResponse<ProductorDto>>

    /**
     * Elimina un productor
     * DELETE /api/productor/{id}
     */
    @DELETE("productor/{id}")
    suspend fun deleteProductor(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    /**
     * Sube una imagen para un productor
     * POST /api/productor/{id}/upload-image
     */
    @Multipart
    @POST("productor/{id}/upload-image")
    suspend fun uploadProductorImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ProductorDto>>
}
