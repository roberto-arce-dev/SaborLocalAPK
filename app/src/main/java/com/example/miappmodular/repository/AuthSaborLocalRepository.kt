package com.example.miappmodular.repository

import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.auth.LoginSaborLocalRequest
import com.example.miappmodular.data.remote.dto.auth.RegisterSaborLocalRequest
import com.example.miappmodular.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para autenticación con SaborLocal API
 *
 * Maneja login, registro y persistencia de sesión.
 * Usa TokenManager de RetrofitClient para gestión segura de tokens.
 *
 * **Arquitectura simple:**
 * Este repository accede directamente a RetrofitClient (singleton)
 * para obtener el API service y el TokenManager.
 * Esto es más fácil de entender para estudiantes que Dependency Injection.
 */
class AuthSaborLocalRepository {

    // Acceso directo a las dependencias desde RetrofitClient
    private val tokenManager = RetrofitClient.getTokenManager()
    private val apiService = RetrofitClient.saborLocalAuthApiService

    /**
     * Verifica si hay una sesión activa
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    /**
     * Obtiene el token JWT guardado
     */
    fun getToken(): String? {
        return tokenManager.getToken()
    }

    /**
     * Guarda el token y datos del usuario usando TokenManager
     */
    private fun saveSession(token: String, user: com.example.miappmodular.data.remote.dto.auth.UserDto) {
        tokenManager.saveToken(token, user)
    }

    /**
     * Obtiene el usuario guardado en sesión
     */
    fun getCurrentUser(): User? {
        return tokenManager.getCurrentUser()
    }

    /**
     * Cierra sesión (elimina token y datos)
     */
    fun logout() {
        tokenManager.clearToken()
    }

    /**
     * Login de usuario
     *
     * @param email Email del usuario
     * @param password Contraseña
     * @return Result con el usuario autenticado
     */
    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = LoginSaborLocalRequest(email, password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val authData = body.data
                    saveSession(authData.accessToken, authData.user)

                    val user = User(
                        id = authData.user.id,
                        nombre = authData.user.nombre,
                        email = authData.user.email,
                        role = authData.user.role,
                        telefono = authData.user.telefono,
                        ubicacion = authData.user.ubicacion,
                        direccion = authData.user.direccion
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception(body?.message ?: "Error en login"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales inválidas"
                    404 -> "Usuario no encontrado"
                    else -> "Error HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        }
    }

    /**
     * Registro de nuevo CLIENTE
     *
     * Solo CLIENTES pueden auto-registrarse.
     * El rol se asigna automáticamente como CLIENTE en el backend.
     *
     * @param nombre Nombre completo
     * @param email Email
     * @param password Contraseña
     * @param telefono Teléfono (opcional)
     * @param direccion Dirección (opcional)
     * @return Result con el usuario registrado
     */
    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        telefono: String? = null,
        direccion: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterSaborLocalRequest(
                nombre = nombre,
                email = email,
                password = password,
                telefono = telefono,
                direccion = direccion
            )
            val response = apiService.register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val authData = body.data
                    saveSession(authData.accessToken, authData.user)

                    val user = User(
                        id = authData.user.id,
                        nombre = authData.user.nombre,
                        email = authData.user.email,
                        role = authData.user.role,
                        telefono = authData.user.telefono,
                        ubicacion = authData.user.ubicacion,
                        direccion = authData.user.direccion
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception(body?.message ?: "Error en registro"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "El email ya está registrado"
                    400 -> "Datos inválidos"
                    else -> "Error HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        }
    }

    /**
     * Crea un nuevo usuario PRODUCTOR (solo ADMIN)
     *
     * Requiere estar autenticado como ADMIN
     *
     * @param nombre Nombre del productor
     * @param email Email del productor
     * @param password Contraseña inicial
     * @param ubicacion Ubicación del productor
     * @param telefono Teléfono del productor
     * @return Result con el usuario creado
     */
    suspend fun createProductorUser(
        nombre: String,
        email: String,
        password: String,
        ubicacion: String,
        telefono: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = com.example.miappmodular.data.remote.dto.auth.CreateProductorUserRequest(
                nombre = nombre,
                email = email,
                password = password,
                ubicacion = ubicacion,
                telefono = telefono
            )
            val response = apiService.createProductorUser(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val authData = body.data
                    // No guardamos la sesión porque el ADMIN sigue logueado

                    val user = User(
                        id = authData.user.id,
                        nombre = authData.user.nombre,
                        email = authData.user.email,
                        role = authData.user.role,
                        telefono = authData.user.telefono,
                        ubicacion = authData.user.ubicacion,
                        direccion = authData.user.direccion
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception(body?.message ?: "Error al crear productor"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    409 -> "El email ya está registrado"
                    403 -> "No tienes permisos para crear productores (requiere rol ADMIN)"
                    400 -> "Datos inválidos"
                    else -> "Error HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        }
    }

    /**
     * Obtiene el perfil del usuario actual
     *
     * Requiere estar autenticado (token en headers)
     *
     * @return Result con el usuario actualizado
     */
    suspend fun getProfile(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val userDto = body.data

                    // Actualizar datos del usuario en TokenManager
                    tokenManager.updateUserData(userDto)

                    val user = User(
                        id = userDto.id,
                        nombre = userDto.nombre,
                        email = userDto.email,
                        role = userDto.role,
                        telefono = userDto.telefono,
                        ubicacion = userDto.ubicacion,
                        direccion = userDto.direccion
                    )
                    Result.success(user)
                } else {
                    Result.failure(Exception(body?.message ?: "Error obteniendo perfil"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    else -> "Error HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        }
    }

    /**
     * Obtiene la lista de todos los usuarios (solo ADMIN)
     *
     * Requiere estar autenticado con rol ADMIN
     *
     * @return Result con la lista de usuarios
     */
    suspend fun getAllUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllUsers()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val users = body.data.map { userDto ->
                        User(
                            id = userDto.id,
                            nombre = userDto.nombre,
                            email = userDto.email,
                            role = userDto.role,
                            telefono = userDto.telefono,
                            ubicacion = userDto.ubicacion,
                            direccion = userDto.direccion
                        )
                    }
                    Result.success(users)
                } else {
                    Result.failure(Exception(body?.message ?: "Error obteniendo usuarios"))
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Sesión expirada"
                    403 -> "No tienes permisos para ver usuarios (requiere rol ADMIN)"
                    else -> "Error HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        }
    }
}
