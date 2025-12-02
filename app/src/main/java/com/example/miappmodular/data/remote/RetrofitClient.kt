package com.example.miappmodular.data.remote

import android.content.Context
import com.example.miappmodular.data.local.TokenManager
import com.example.miappmodular.data.remote.api.*
import com.example.miappmodular.data.remote.dto.pedido.ClienteDto
import com.example.miappmodular.data.remote.dto.pedido.ClienteDtoDeserializer
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Objeto singleton que configura y proporciona el cliente Retrofit para la API de SaborLocal.
 *
 * **Patrón Singleton Simple:**
 * Este object usa el patrón Singleton de Kotlin para garantizar una única instancia
 * compartida de Retrofit en toda la aplicación.
 *
 * **¿Por qué necesitamos initialize()?**
 * Necesitamos el Context de la aplicación para crear TokenManager, que usa
 * EncryptedSharedPreferences para guardar tokens de forma segura.
 *
 * **Configuración:**
 * - **Base URL:** http://10.0.2.2:3008/api/ (localhost del emulador)
 * - **TokenManager:** Gestión segura de tokens JWT con encriptación
 * - **AuthInterceptor:** Añade automáticamente el token a las peticiones
 * - **Logging:** Registra requests/responses para debugging
 * - **Timeouts:** Aumentados para Render.com (60s conexión, 90s lectura, 60s escritura)
 *
 * **Ejemplo de uso en MainActivity:**
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     override fun onCreate(savedInstanceState: Bundle?) {
 *         super.onCreate(savedInstanceState)
 *
 *         // Inicializar RetrofitClient una sola vez al inicio
 *         RetrofitClient.initialize(this)
 *
 *         setContent { /* ... */ }
 *     }
 * }
 * ```
 *
 * **Ejemplo de uso en Repository:**
 * ```kotlin
 * class ProductoRepository {
 *     private val apiService = RetrofitClient.saborLocalProductoApiService
 *
 *     suspend fun getProductos(): Result<List<Producto>> {
 *         val response = apiService.getProductos()
 *         // ...
 *     }
 * }
 * ```
 *
 * @see TokenManager
 * @see AuthInterceptor
 */
object RetrofitClient {

    /**
     * URL base de la API de SaborLocal.
     *
     * **NOTA:** 10.0.2.2 es la IP especial que el emulador de Android usa
     * para acceder a localhost de tu máquina host.
     *
     * Si usas un dispositivo físico, cambia esto por la IP de tu computadora
     * en la red local (ej: 192.168.1.X:3008).
     */
    private const val BASE_URL = "https://saborloca-api.onrender.com/api/"

    /**
     * TokenManager para gestión segura de tokens JWT.
     * Se inicializa llamando a initialize() con el contexto de la app.
     */
    private lateinit var tokenManager: TokenManager

    /**
     * Inicializa el RetrofitClient con el contexto de la aplicación.
     *
     * **IMPORTANTE:** Debe llamarse UNA SOLA VEZ al inicio de la app,
     * en MainActivity.onCreate() o en Application.onCreate().
     *
     * @param context Contexto de la aplicación (preferiblemente ApplicationContext)
     *
     * Ejemplo:
     * ```kotlin
     * class MainActivity : ComponentActivity() {
     *     override fun onCreate(savedInstanceState: Bundle?) {
     *         super.onCreate(savedInstanceState)
     *         RetrofitClient.initialize(this)
     *     }
     * }
     * ```
     */
    fun initialize(context: Context) {
        tokenManager = TokenManager(context.applicationContext)
    }

    /**
     * Cliente HTTP OkHttp configurado con interceptores y timeouts.
     *
     * **Configuración:**
     * 1. **AuthInterceptor:** Añade automáticamente el header Authorization con el JWT token
     * 2. **HttpLoggingInterceptor:** Registra todas las peticiones y respuestas (útil para debugging)
     * 3. **Timeouts aumentados para Render.com free tier:**
     *    - connectTimeout: 60s (para cold starts)
     *    - readTimeout: 90s (para operaciones lentas)
     *    - writeTimeout: 60s (para uploads grandes)
     *
     * **Lazy initialization:**
     * Se crea solo cuando se accede por primera vez. Thread-safe por defecto en Kotlin.
     */
    private val okHttpClient: OkHttpClient by lazy {
        // AuthInterceptor añade el token JWT a las peticiones automáticamente
        val authInterceptor = AuthInterceptor(tokenManager)

        // LoggingInterceptor registra las peticiones y respuestas para debugging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)  // Aumentado para cold starts de Render
            .readTimeout(90, TimeUnit.SECONDS)     // Aumentado para operaciones lentas
            .writeTimeout(60, TimeUnit.SECONDS)    // Aumentado para uploads grandes
            .build()
    }

    /**
     * Configuración personalizada de Gson con deserializadores custom.
     *
     * **ClienteDtoDeserializer:**
     * Maneja el caso donde el backend retorna `cliente` como String en listas
     * y como Object en detalles (debido a .populate() de MongoDB).
     */
    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(ClienteDto::class.java, ClienteDtoDeserializer())
            .create()
    }

    /**
     * Instancia singleton de Retrofit.
     *
     * Retrofit convierte interfaces Kotlin en clientes HTTP funcionales,
     * manejando automáticamente:
     * - Serialización JSON ↔ Objetos Kotlin (con Gson)
     * - Manejo de URLs, headers y parámetros
     * - Integración con corrutinas (suspend functions)
     *
     * **Lazy initialization:**
     * Se crea solo cuando se necesita por primera vez.
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // ========= API Services - Organizados por dominio =========

    /**
     * API service para autenticación (login, registro, perfil)
     */
    val saborLocalAuthApiService: SaborLocalAuthApiService by lazy {
        retrofit.create(SaborLocalAuthApiService::class.java)
    }

    /**
     * API service para gestión de productos
     */
    val saborLocalProductoApiService: SaborLocalProductoApiService by lazy {
        retrofit.create(SaborLocalProductoApiService::class.java)
    }

    /**
     * API service para gestión de productores
     */
    val saborLocalProductorApiService: SaborLocalProductorApiService by lazy {
        retrofit.create(SaborLocalProductorApiService::class.java)
    }

    /**
     * API service para gestión de clientes
     */
    val saborLocalClienteApiService: SaborLocalClienteApiService by lazy {
        retrofit.create(SaborLocalClienteApiService::class.java)
    }

    /**
     * API service para gestión de pedidos
     */
    val saborLocalPedidoApiService: SaborLocalPedidoApiService by lazy {
        retrofit.create(SaborLocalPedidoApiService::class.java)
    }

    /**
     * API service para gestión de entregas
     */
    val saborLocalEntregaApiService: SaborLocalEntregaApiService by lazy {
        retrofit.create(SaborLocalEntregaApiService::class.java)
    }

    /**
     * API service para subir archivos (imágenes, documentos)
     */
    val saborLocalUploadApiService: SaborLocalUploadApiService by lazy {
        retrofit.create(SaborLocalUploadApiService::class.java)
    }

    /**
     * Obtiene el TokenManager para acceso directo al token.
     *
     * Útil en casos donde necesitas verificar si hay sesión activa
     * o acceder al usuario actual sin pasar por el repository.
     *
     * Ejemplo:
     * ```kotlin
     * val isLoggedIn = RetrofitClient.getTokenManager().isLoggedIn()
     * val currentUser = RetrofitClient.getTokenManager().getCurrentUser()
     * ```
     */
    fun getTokenManager(): TokenManager = tokenManager
}
