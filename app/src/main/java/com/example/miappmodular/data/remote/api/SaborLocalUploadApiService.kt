package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de Upload de SaborLocal
 */
interface SaborLocalUploadApiService {

    /**
     * Sube una imagen independiente (sin asociar a entidad)
     * POST /api/upload/image
     */
    @Multipart
    @POST("upload/image")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<Map<String, String>>>
}
