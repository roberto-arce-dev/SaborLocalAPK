package com.example.miappmodular.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.miappmodular.data.local.SessionManager
import kotlinx.coroutines.runBlocking
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
 * 2. Recupera el authToken de SaborLocal SharedPreferences o SessionManager
 * 3. Añade el header `Authorization: Bearer {token}` si el token existe
 * 4. Permite que la petición continúe normalmente
 *
 * **Prioridad de tokens:**
 * 1. Primero intenta obtener el token de SaborLocal (SharedPreferences)
 * 2. Si no existe, intenta obtener el token de SessionManager (DataStore)
 *
 * @property sessionManager Gestor de sesión que proporciona el authToken de Xano.
 * @property context Contexto para acceder a SharedPreferences de SaborLocal.
 *
 * @see SessionManager
 * @see RetrofitClient
 * @see AuthApiService
 */
class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val context: Context
) : Interceptor {

    private val saborLocalPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("saborlocal_prefs", Context.MODE_PRIVATE)
    }

    /**
     * Intercepta y modifica la petición HTTP para añadir autenticación.
     *
     * **Flujo de ejecución:**
     * 1. Intenta obtener token de SaborLocal (SharedPreferences)
     * 2. Si no existe, intenta obtener token de SessionManager (DataStore)
     * 3. Si existe token, crea una nueva petición con header Authorization
     * 4. Si no hay token, deja la petición sin modificar
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

        // Intentar obtener token de SaborLocal primero
        var token = saborLocalPrefs.getString("auth_token", null)

        // Si no hay token de SaborLocal, intentar con SessionManager (Xano)
        if (token.isNullOrEmpty()) {
            token = runBlocking {
                sessionManager.getAuthToken()
            }
        }

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