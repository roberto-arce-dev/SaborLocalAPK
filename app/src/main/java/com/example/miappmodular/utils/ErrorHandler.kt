package com.example.miappmodular.utils

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Gestor centralizado de errores para la aplicación.
 *
 * **¿Por qué centralizar el manejo de errores?**
 * - Mensajes de error consistentes en toda la app
 * - Fácil de traducir/localizar mensajes
 * - Mejor UX con mensajes user-friendly
 * - Logging centralizado para debugging
 *
 * **Uso en Repositories:**
 * ```kotlin
 * catch (e: Exception) {
 *     ApiResult.Error(ErrorHandler.getErrorMessage(e), e)
 * }
 * ```
 *
 * **Uso en ViewModels:**
 * ```kotlin
 * is ApiResult.Error -> {
 *     val userMessage = ErrorHandler.getUserFriendlyMessage(result.message)
 *     UiState.Error(userMessage)
 * }
 * ```
 */
object ErrorHandler {

    /**
     * Convierte una excepción en un mensaje user-friendly.
     *
     * @param exception La excepción capturada
     * @return Mensaje de error apropiado para mostrar al usuario
     */
    fun getErrorMessage(exception: Throwable): String {
        return when (exception) {
            is UnknownHostException -> "Sin conexión a internet. Por favor verifica tu conexión."
            is SocketTimeoutException -> "La conexión tardó demasiado. Intenta de nuevo."
            is IOException -> "Error de conexión. Verifica tu internet e intenta de nuevo."
            else -> "Error: ${exception.message ?: "Algo salió mal"}"
        }
    }

    /**
     * Convierte mensajes técnicos de error en mensajes amigables para el usuario.
     *
     * @param technicalMessage Mensaje técnico del error (de la API o excepción)
     * @return Mensaje user-friendly
     */
    fun getUserFriendlyMessage(technicalMessage: String): String {
        return when {
            // Errores de red
            technicalMessage.contains("Error de red", ignoreCase = true) ->
                "Sin conexión a internet. Verifica tu conexión."

            technicalMessage.contains("Error de conexión", ignoreCase = true) ->
                "No se pudo conectar al servidor. Intenta de nuevo."

            technicalMessage.contains("timeout", ignoreCase = true) ->
                "La operación tardó demasiado. Por favor intenta de nuevo."

            // Errores de autenticación
            technicalMessage.contains("credenciales inválidas", ignoreCase = true) ->
                "Email o contraseña incorrectos"

            technicalMessage.contains("sesión expirada", ignoreCase = true) ->
                "Tu sesión ha expirado. Por favor inicia sesión nuevamente."

            technicalMessage.contains("no autorizado", ignoreCase = true) ->
                "No tienes permisos para realizar esta acción"

            // Errores de validación
            technicalMessage.contains("email ya está registrado", ignoreCase = true) ->
                "Este email ya está en uso. Intenta con otro."

            technicalMessage.contains("datos inválidos", ignoreCase = true) ->
                "Por favor verifica los datos ingresados"

            // Errores de recursos
            technicalMessage.contains("no encontrado", ignoreCase = true) ->
                "El recurso solicitado no existe"

            technicalMessage.contains("stock insuficiente", ignoreCase = true) ->
                "No hay suficiente stock disponible"

            // Error genérico
            else -> technicalMessage
        }
    }

    /**
     * Extrae el código HTTP de un mensaje de error y retorna un mensaje apropiado.
     *
     * @param errorMessage Mensaje que puede contener "Error HTTP XXX"
     * @return Mensaje user-friendly basado en el código HTTP
     */
    fun getHttpErrorMessage(errorMessage: String): String {
        val httpCodeRegex = Regex("HTTP (\\d{3})")
        val match = httpCodeRegex.find(errorMessage)

        return match?.groupValues?.get(1)?.toIntOrNull()?.let { code ->
            when (code) {
                400 -> "Solicitud inválida. Verifica los datos ingresados."
                401 -> "No estás autenticado. Por favor inicia sesión."
                403 -> "No tienes permisos para esta acción."
                404 -> "El recurso no fue encontrado."
                409 -> "Ya existe un registro con estos datos."
                422 -> "Datos de entrada inválidos. Verifica el formulario."
                429 -> "Demasiados intentos. Espera un momento e intenta de nuevo."
                500 -> "Error del servidor. Intenta más tarde."
                502, 503 -> "Servicio no disponible temporalmente. Intenta más tarde."
                504 -> "El servidor no responde. Intenta más tarde."
                else -> errorMessage
            }
        } ?: getUserFriendlyMessage(errorMessage)
    }

    /**
     * Registra errores en el log para debugging (producción usaría Crashlytics/Sentry).
     *
     * @param tag Tag para identificar el origen del error
     * @param message Mensaje descriptivo
     * @param exception Excepción opcional
     */
    fun logError(tag: String, message: String, exception: Throwable? = null) {
        if (exception != null) {
            android.util.Log.e(tag, message, exception)
        } else {
            android.util.Log.e(tag, message)
        }
    }
}
