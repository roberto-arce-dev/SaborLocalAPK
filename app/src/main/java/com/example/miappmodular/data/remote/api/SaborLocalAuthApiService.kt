package com.example.miappmodular.data.remote.api

import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.data.remote.dto.auth.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para endpoints de autenticación de SaborLocal
 */
interface SaborLocalAuthApiService {

    /**
     * Login de usuario
     * POST /api/auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginSaborLocalRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    /**
     * Registro de nuevo CLIENTE
     * POST /api/auth/register
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterSaborLocalRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    /**
     * Obtener perfil del usuario actual
     * GET /api/auth/profile
     */
    @GET("auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    /**
     * Listar todos los usuarios (Solo ADMIN)
     * GET /api/auth/users
     */
    @GET("auth/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserDto>>>

    /**
     * Crea un nuevo usuario PRODUCTOR (solo ADMIN)
     * POST /api/auth/create-productor
     */
    @POST("auth/create-productor")
    suspend fun createProductorUser(
        @Body request: CreateProductorUserRequest
    ): Response<ApiResponse<AuthSaborLocalData>>
}
