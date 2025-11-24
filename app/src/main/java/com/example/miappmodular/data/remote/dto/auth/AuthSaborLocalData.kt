package com.example.miappmodular.data.remote.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * Response de autenticación de SaborLocal
 * Retornado por login y register
 */
data class AuthSaborLocalData(
    val user: UserDto,
    @SerializedName("access_token")
    val accessToken: String
)
