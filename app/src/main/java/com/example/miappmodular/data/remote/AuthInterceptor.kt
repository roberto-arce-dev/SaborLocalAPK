package com.example.miappmodular.data.remote

import com.example.miappmodular.data.local.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor OkHttp que añade automáticamente el token de autenticación JWT
 * a todas las peticiones HTTP que requieran autorización.
 *
 * Este interceptor implementa el patrón de "Token Injection", permitiendo
 * centralizar la lógica de autenticación en un solo lugar en vez de tener
 * que añadir manualmente el header en cada llamada a la API.
 *
 * **Funcionamiento:**
 * 1. Intercepta todas las peticiones HTTP antes de enviarlas al servidor
 * 2. Recupera el authToken de TokenManager (EncryptedSharedPreferences)
 * 3. Añade el header `Authorization: Bearer {token}` si el token existe
 * 4. Permite que la petición continúe normalmente
 *
 * **¿Por qué TokenManager es sincrónico?**
 * - Los interceptores de OkHttp se ejecutan en threads de red
 * - No pueden usar suspend functions sin bloquear el thread pool
 * - TokenManager usa EncryptedSharedPreferences (sincrónico) en lugar de DataStore (async)
 * - Esto evita `runBlocking()` que degradaría el rendimiento de red
 *
 * @property tokenManager Gestor de tokens JWT inyectado por Hilt
 *
 * @see TokenManager
 * @see NetworkModule
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {

    /**
     * Intercepta y modifica la petición HTTP para añadir autenticación.
     *
     * **Flujo de ejecución:**
     * 1. Obtiene el token JWT de TokenManager (operación sincrónica)
     * 2. Si existe token, crea una nueva petición con header Authorization
     * 3. Si no hay token, deja la petición sin modificar
     *
     * **Header generado:**
     * ```
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     * ```
     *
     * @param chain Cadena de interceptores de OkHttp.
     * @return Response del servidor tras ejecutar la petición.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Obtener token de TokenManager (sincrónico, no bloquea threads)
        val token = tokenManager.getToken()

        // Si no hay token, continuar con la petición original sin modificar
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // Crear nueva petición con el header Authorization
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        // Continuar con la petición autenticada
        return chain.proceed(authenticatedRequest)
    }
}