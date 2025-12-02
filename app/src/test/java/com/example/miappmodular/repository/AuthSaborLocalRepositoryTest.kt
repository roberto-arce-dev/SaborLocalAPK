package com.example.miappmodular.repository

import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.auth.AuthSaborLocalData
import com.example.miappmodular.data.remote.dto.auth.UserDto
import com.example.miappmodular.data.remote.dto.auth.LoginSaborLocalRequest
import com.example.miappmodular.data.remote.dto.common.ApiResponse
import com.example.miappmodular.model.ApiResult
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests para AuthSaborLocalRepository
 *
 * **Arquitectura de tests simple:**
 * Mockea RetrofitClient para interceptar las llamadas al API service y TokenManager.
 * Los estudiantes pueden ver claramente qué se está mockeando y por qué.
 */
class AuthSaborLocalRepositoryTest {

    // SUT (System Under Test)
    private lateinit var repository: AuthSaborLocalRepository

    // Mocks que se reutilizan en todos los tests
    private lateinit var mockApiService: com.example.miappmodular.data.remote.api.SaborLocalAuthApiService
    private lateinit var mockTokenManager: com.example.miappmodular.data.local.TokenManager

    /**
     * Función para crear el setup de nuestros mocks
     *
     * Mockea RetrofitClient para controlar:
     * - El API service que retorna respuestas simuladas
     * - El TokenManager que guarda tokens en memoria
     */
    @Before
    fun setup() {
        // Mockear el objeto singleton RetrofitClient
        mockkObject(RetrofitClient)

        // Crear los mocks de las dependencias
        mockApiService = mockk()
        mockTokenManager = mockk(relaxed = true)

        // Configurar RetrofitClient para usar nuestros mocks ANTES de crear el repository
        every { RetrofitClient.saborLocalAuthApiService } returns mockApiService
        every { RetrofitClient.getTokenManager() } returns mockTokenManager

        // El repository se crea normalmente, pero usará los mocks de RetrofitClient
        repository = AuthSaborLocalRepository()
    }


    /**
     * Funcion que limpiara nuestros mocks
     */
    @After
    fun teardown(){
        unmockkAll()
    }


    // ========= Login Test ============

    @Test
    fun `Login exitoso debe retornar Success con User` () = runTest {

        // Primero definimos nuestras variables de entrada
        val email = "test@example.com"
        val password = "password123"

        // Definir la respuesta esperada del API
        val userDto = UserDto(
            id = "user123",
            nombre = "Test User",
            email = email,
            role = "CLIENTE"
        )

        val authData = AuthSaborLocalData(
            user = userDto,
            accessToken = "mock_token_12345"
        )

        val apiResponse = ApiResponse(
            success = true,
            message = "Login Exitoso",
            data = authData
        )

        val response = Response.success(apiResponse)

        // Configurar el mock del API service para que retorne nuestra respuesta simulada
        coEvery {
            mockApiService.login(LoginSaborLocalRequest(email, password))
        } returns response

        // Ejecutar el método que queremos probar
        val result = repository.login(email, password)

        // Verificar que el resultado es exitoso
        assertTrue(result.isSuccess, "El resultado debe ser Success")

        // Verificar que los datos del usuario son correctos
        val user = result.getOrNull()
        assertEquals("user123", user?.id)
        assertEquals("Test User", user?.nombre)
        assertEquals(email, user?.email)
        assertEquals("CLIENTE", user?.role)

        // Verificar que se guardó el token en TokenManager
        verify { mockTokenManager.saveToken("mock_token_12345", userDto) }
    }
}