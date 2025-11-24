package com.example.miappmodular.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.miappmodular.data.remote.api.SaborLocalAuthApiService
import com.example.miappmodular.data.remote.dto.auth.AuthSaborLocalData
import com.example.miappmodular.data.remote.dto.auth.UserDto
import com.example.miappmodular.data.remote.dto.auth.LoginSaborLocalRequest
import com.example.miappmodular.data.remote.dto.common.ApiResponse
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthSaborLocalRepositoryTest {

    //mocks
    private lateinit var mockContext: Context
    private lateinit var mockApiService: SaborLocalAuthApiService
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor : SharedPreferences.Editor

    // SUT
    private lateinit var repository: AuthSaborLocalRepository

    /**
     * Función para crear el setup de nuestras mocks
     * @see com.example.miappmodular.data.remote.RetrofitClient
     */
    @Before
    fun setup() {

        //crear mocks
        mockContext = mockk(relaxed= true)
        mockApiService = mockk<SaborLocalAuthApiService>()
        mockSharedPreferences=mockk(relaxed= true)
        mockEditor= mockk(relaxed= true)

        // configurar comportamiento de sharedpreferece

        every { mockContext.getSharedPreferences(any(),any()) } returns mockSharedPreferences
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(),any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockEditor.clear() } returns mockEditor

        // Mockear Retrofit Client para el uso de nuestro mock
        mockkObject(com.example.miappmodular.data.remote.RetrofitClient)
        every { com.example.miappmodular.data.remote.RetrofitClient.saborLocalAuthApiService } returns mockApiService

        repository = AuthSaborLocalRepository(mockContext)

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

        // Primero definimos nuestras variables
        val email = "test@example.com"
        val password = "password123"


        val userDto = UserDto(
            id = "user123",
            nombre = "Test User",
            email = email,
            role = "CLIENTE"
        )

        val authData = AuthSaborLocalData (
            user= userDto,
            accessToken = "mock_token_12345"
        )

        val apiResponse = ApiResponse(
           success = true,
            message = "Login Exitoso",
            data = authData
        )

        val reponse = Response.success(apiResponse)

        coEvery {
            mockApiService.login(LoginSaborLocalRequest(email,password))
        } returns reponse

        val result = repository.login(email,password)

        assertTrue ( result.isSuccess,"El resultado debe ser Success")

        val user = result.getOrNull()
        assertEquals("user123", user?.id)
        assertEquals("Test User", user?.nombre)
        assertEquals(email, user?.email)
        assertEquals("CLIENTE", user?.role)

        // Verificar que se guardó el token
        verify { mockEditor.putString("auth_token", "mock_token_12345") }
        verify { mockEditor.putString("user_id", "user123") }
        verify { mockEditor.apply() }


    }
}