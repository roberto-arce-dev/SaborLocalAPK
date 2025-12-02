package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.data.remote.dto.producto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de Productos de SaborLocal
 */
interface SaborLocalProductoApiService {

    /**
     * Obtiene la lista de todos los productos
     * GET /api/producto
     */
    @GET("producto")
    suspend fun getProductos(): Response<ApiResponse<List<ProductoDto>>>

    /**
     * Obtiene el catálogo de productos de un productor específico
     * GET /api/producto/productor/{productorId}
     *
     * Endpoint clave para EP3: Permite ver todos los productos que ofrece un productor
     */
    @GET("producto/productor/{productorId}")
    suspend fun getProductosByProductor(
        @Path("productorId") productorId: String
    ): Response<ApiResponse<List<ProductoDto>>>

    /**
     * Obtiene un producto específico por ID
     * GET /api/producto/{id}
     */
    @GET("producto/{id}")
    suspend fun getProducto(
        @Path("id") id: String
    ): Response<ApiResponse<ProductoDto>>

    /**
     * Crea un nuevo producto
     * POST /api/producto
     */
    @POST("producto")
    suspend fun createProducto(
        @Body request: CreateProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    /**
     * Actualiza un producto existente
     * PATCH /api/producto/{id}
     */
    @PATCH("producto/{id}")
    suspend fun updateProducto(
        @Path("id") id: String,
        @Body request: UpdateProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    /**
     * Elimina un producto
     * DELETE /api/producto/{id}
     */
    @DELETE("producto/{id}")
    suspend fun deleteProducto(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    /**
     * Sube una imagen para un producto
     * POST /api/producto/{id}/upload-image
     */
    @Multipart
    @POST("producto/{id}/upload-image")
    suspend fun uploadProductoImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ProductoDto>>
}
