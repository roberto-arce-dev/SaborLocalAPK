# Tutorial Paso a Paso - Mi App Modular
## Aplicación Android con Kotlin, Jetpack Compose y Arquitectura MVVM

**Autor:** Roberto
**Curso:** Desarrollo de Aplicaciones Android
**Tecnologías:** Kotlin, Jetpack Compose, Retrofit, Coroutines, MVVM

---

## 📋 Tabla de Contenidos

1. [Introducción](#1-introducción)
2. [Configuración del Proyecto](#2-configuración-del-proyecto)
3. [Arquitectura MVVM](#3-arquitectura-mvvm)
4. [Capa de Datos (Data Layer)](#4-capa-de-datos-data-layer)
5. [Capa de Dominio (Domain Layer)](#5-capa-de-dominio-domain-layer)
6. [Capa de Presentación (Presentation Layer)](#6-capa-de-presentación-presentation-layer)
7. [Sistema de Navegación](#7-sistema-de-navegación)
8. [Autenticación y Sesión](#8-autenticación-y-sesión)
9. [Gestión de Productos y Productores](#9-gestión-de-productos-y-productores)
10. [Características Avanzadas](#10-características-avanzadas)
11. [Buenas Prácticas Implementadas](#11-buenas-prácticas-implementadas)

---

## 1. Introducción

### ¿Qué vamos a construir?

Una aplicación de marketplace agrícola llamada **"SaborLocal"** que permite:

- ✅ Registro e inicio de sesión de usuarios
- ✅ 3 roles de usuario: CLIENTE, PRODUCTOR, ADMIN
- ✅ Gestión de productos agrícolas
- ✅ Gestión de productores
- ✅ Sistema de autenticación con JWT
- ✅ Arquitectura limpia y escalable

### Tecnologías Utilizadas

| Tecnología | Propósito |
|------------|-----------|
| **Kotlin** | Lenguaje de programación |
| **Jetpack Compose** | UI moderna y declarativa |
| **Retrofit** | Cliente HTTP para APIs |
| **OkHttp** | Interceptores y logging |
| **Coroutines** | Programación asíncrona |
| **StateFlow** | Estado reactivo |
| **SharedPreferences** | Persistencia local |
| **Material3** | Componentes de diseño |

---

## 2. Configuración del Proyecto

### Paso 1: Crear Nuevo Proyecto

1. Abrir Android Studio
2. **File → New → New Project**
3. Seleccionar **Empty Activity (Compose)**
4. Configurar:
   - Name: `MiAppModular`
   - Package: `com.example.miappmodular`
   - Language: `Kotlin`
   - Minimum SDK: `24 (Android 7.0)`
   - Build configuration: `Kotlin DSL (build.gradle.kts)`

### Paso 2: Agregar Dependencias

**Archivo:** `app/build.gradle.kts`

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

    // Retrofit - Cliente HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

### Paso 3: Configurar Permisos

**Archivo:** `app/src/main/AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Permisos de Internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".MyApplication"
        android:networkSecurityConfig="@xml/network_security_config"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MiAppModular">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### Paso 4: Configurar Network Security (para desarrollo)

**Archivo:** `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Permitir HTTP solo en desarrollo (localhost) -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>

    <!-- Configuración base para producción (solo HTTPS) -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

**⚠️ Importante:** En producción, **NUNCA** uses `cleartextTrafficPermitted="true"`. Solo es para desarrollo con APIs locales.

---

## 3. Arquitectura MVVM

### ¿Qué es MVVM?

**MVVM** (Model-View-ViewModel) es un patrón arquitectónico que separa la lógica de negocio de la UI:

```
┌──────────────────────────────────────────┐
│              VIEW (Compose)              │
│  - Screens (LoginScreen, HomeScreen)    │
│  - Muestra datos del ViewModel          │
│  - Maneja interacciones del usuario     │
└──────────────────────────────────────────┘
                    ↕ observa StateFlow
┌──────────────────────────────────────────┐
│            VIEWMODEL                     │
│  - Maneja estado de UI                  │
│  - Lógica de presentación                │
│  - Llama al Repository                   │
└──────────────────────────────────────────┘
                    ↕ usa
┌──────────────────────────────────────────┐
│            REPOSITORY                    │
│  - Abstrae fuente de datos               │
│  - Decide: API o caché local             │
└──────────────────────────────────────────┘
                    ↕ usa
┌──────────────────────────────────────────┐
│         DATA SOURCES                     │
│  - Remote: Retrofit API                  │
│  - Local: SharedPreferences/DataStore   │
└──────────────────────────────────────────┘
```

### Estructura de Carpetas del Proyecto

```
app/src/main/java/com/example/miappmodular/
│
├── data/                           # Capa de Datos
│   ├── local/                      # Fuentes de datos locales
│   │   └── SessionManager.kt       # Gestión de sesión (DataStore)
│   │
│   └── remote/                     # Fuentes de datos remotas
│       ├── AuthInterceptor.kt      # Interceptor JWT
│       ├── RetrofitClient.kt       # Configuración Retrofit
│       ├── AuthApiService.kt       # API Xano (vieja)
│       ├── SaborLocalApiService.kt # API SaborLocal (nueva)
│       │
│       └── dto/                    # Data Transfer Objects
│           └── SaborLocalDtos.kt   # DTOs del backend
│
├── model/                          # Capa de Dominio
│   └── SaborLocalModels.kt         # Modelos de dominio
│
├── repository/                     # Repositorios
│   ├── UserRepository.kt           # Repo viejo (Xano)
│   ├── AuthSaborLocalRepository.kt # Repo autenticación
│   ├── ProductoRepository.kt       # Repo productos
│   └── ProductorRepository.kt      # Repo productores
│
├── viewmodel/                      # ViewModels
│   ├── LoginViewModel.kt           # VM Login
│   ├── RegisterViewModel.kt        # VM Registro
│   ├── HomeViewModel.kt            # VM Home
│   ├── ProfileViewModel.kt         # VM Perfil
│   ├── SplashViewModel.kt          # VM Splash
│   ├── CreateProductoViewModel.kt  # VM Crear Producto
│   ├── CreateProductorViewModel.kt # VM Crear Productor
│   ├── ProductosListViewModel.kt   # VM Lista Productos
│   └── ProductoresListViewModel.kt # VM Lista Productores
│
├── ui/                             # Capa de Presentación
│   ├── screens/                    # Pantallas
│   │   ├── SplashScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── ProductosListScreen.kt
│   │   ├── CreateProductoScreen.kt
│   │   ├── CreateProductorScreen.kt
│   │   └── ProductoresListScreen.kt
│   │
│   ├── components/                 # Componentes reutilizables
│   │   └── ShadcnComponents.kt     # Componentes estilo shadcn
│   │
│   ├── navigation/                 # Navegación
│   │   └── AppNavigation.kt        # Grafo de navegación
│   │
│   └── theme/                      # Tema
│       └── ShadcnTheme.kt          # Colores shadcn
│
├── utils/                          # Utilidades
│   └── ValidationUtils.kt          # Validaciones
│
└── MainActivity.kt                 # Activity principal
```

---

## 4. Capa de Datos (Data Layer)

### 4.1. DTOs (Data Transfer Objects)

Los DTOs representan exactamente la estructura JSON que retorna el backend.

**Archivo:** `data/remote/dto/SaborLocalDtos.kt`

```kotlin
package com.example.miappmodular.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Wrapper genérico para respuestas del API
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

/**
 * DTO para Usuario
 */
data class UserDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val email: String,
    val role: String,  // CLIENTE, PRODUCTOR, ADMIN
    val telefono: String? = null,
    val ubicacion: String? = null,
    val direccion: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

/**
 * Response de autenticación
 */
data class AuthSaborLocalData(
    val user: UserDto,
    @SerializedName("access_token")
    val accessToken: String
)

/**
 * Request para login
 */
data class LoginSaborLocalRequest(
    val email: String,
    val password: String
)

/**
 * Request para registro de CLIENTE
 */
data class RegisterSaborLocalRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null
)

/**
 * Request para crear PRODUCTOR (solo ADMIN)
 */
data class CreateProductorUserRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val ubicacion: String,
    val telefono: String
)

/**
 * DTO para Producto
 */
data class ProductoDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: Any,  // Puede ser ID o objeto completo
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

/**
 * Request para crear producto
 */
data class CreateProductoRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: String  // ID del productor
)

/**
 * DTO para Productor (tabla separada - NO USADA)
 */
data class ProductorDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String
)
```

**💡 Conceptos Clave:**

- **@SerializedName**: Mapea campos JSON a propiedades Kotlin
  ```kotlin
  @SerializedName("_id")  // En JSON: "_id"
  val id: String          // En Kotlin: "id"
  ```

- **Nullable vs Non-null**:
  - `val nombre: String` → Requerido, falla si es null
  - `val telefono: String?` → Opcional, puede ser null

- **Generic Type**:
  ```kotlin
  ApiResponse<AuthSaborLocalData>  // data contiene AuthSaborLocalData
  ApiResponse<List<UserDto>>       // data contiene lista de UserDto
  ```

### 4.2. API Service (Retrofit)

**Archivo:** `data/remote/SaborLocalApiService.kt`

```kotlin
package com.example.miappmodular.data.remote

import com.example.miappmodular.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para SaborLocal Backend
 * Base URL: http://10.0.2.2:3008/api/
 */
interface SaborLocalApiService {

    // ==================== AUTHENTICATION ====================

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
     * Requiere: Bearer token en header
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
     * Crear usuario PRODUCTOR (Solo ADMIN)
     * POST /api/auth/create-productor
     */
    @POST("auth/create-productor")
    suspend fun createProductorUser(
        @Body request: CreateProductorUserRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    // ==================== PRODUCTOS ====================

    /**
     * Obtener todos los productos
     * GET /api/producto
     */
    @GET("producto")
    suspend fun getProductos(): Response<ApiResponse<List<ProductoDto>>>

    /**
     * Crear producto (Solo PRODUCTOR)
     * POST /api/producto
     */
    @POST("producto")
    suspend fun createProducto(
        @Body request: CreateProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    /**
     * Eliminar producto
     * DELETE /api/producto/{id}
     */
    @DELETE("producto/{id}")
    suspend fun deleteProducto(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>

    // ==================== PRODUCTORES ====================

    /**
     * Obtener todos los productores
     * GET /api/productor
     */
    @GET("productor")
    suspend fun getProductores(): Response<ApiResponse<List<ProductorDto>>>

    /**
     * Eliminar productor
     * DELETE /api/productor/{id}
     */
    @DELETE("productor/{id}")
    suspend fun deleteProductor(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
```

**💡 Anotaciones de Retrofit:**

| Anotación | Significado | Ejemplo |
|-----------|-------------|---------|
| `@GET` | HTTP GET | `@GET("users")` |
| `@POST` | HTTP POST | `@POST("auth/login")` |
| `@DELETE` | HTTP DELETE | `@DELETE("product/{id}")` |
| `@Path` | Parámetro en URL | `/product/{id}` |
| `@Body` | Body del request | JSON en POST |
| `@Query` | Query param | `?page=1` |

### 4.3. Retrofit Client (Singleton)

**Archivo:** `data/remote/RetrofitClient.kt`

```kotlin
package com.example.miappmodular.data.remote

import android.content.Context
import com.example.miappmodular.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configuración global de Retrofit (Singleton)
 */
object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:3008/api/"

    private lateinit var sessionManager: SessionManager
    private lateinit var context: Context

    /**
     * Inicializar antes de usar
     * Llamar en Application.onCreate()
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
        sessionManager = SessionManager(context.applicationContext)
    }

    /**
     * OkHttpClient con interceptores
     */
    private val okHttpClient: OkHttpClient by lazy {
        // Interceptor para añadir JWT token
        val authInterceptor = AuthInterceptor(sessionManager, context)

        // Interceptor para logging (debugging)
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instancia de Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * API Service
     */
    val saborLocalApiService: SaborLocalApiService by lazy {
        retrofit.create(SaborLocalApiService::class.java)
    }
}
```

**💡 Explicación:**

1. **Singleton Pattern**: `object RetrofitClient` → una sola instancia global
2. **Lazy initialization**: `by lazy` → se crea solo cuando se usa
3. **Interceptores**: Se ejecutan antes de cada request
4. **Timeouts**: Previenen requests eternos

### 4.4. Auth Interceptor (JWT)

**Archivo:** `data/remote/AuthInterceptor.kt`

```kotlin
package com.example.miappmodular.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.miappmodular.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor que añade automáticamente el token JWT
 * a todas las peticiones HTTP
 */
class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val context: Context
) : Interceptor {

    private val saborLocalPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("saborlocal_prefs", Context.MODE_PRIVATE)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. Intentar obtener token de SaborLocal (SharedPreferences)
        var token = saborLocalPrefs.getString("auth_token", null)

        // 2. Fallback: SessionManager (DataStore - API vieja)
        if (token.isNullOrEmpty()) {
            token = runBlocking {
                sessionManager.getAuthToken()
            }
        }

        // 3. Si no hay token, continuar sin modificar
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }

        // 4. Añadir header Authorization
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(authenticatedRequest)
    }
}
```

**💡 Flujo del Interceptor:**

```
Request Original
      ↓
¿Hay token en SharedPreferences?
   Sí ↓         No ↓
   Token    ¿Hay token en SessionManager?
              Sí ↓         No ↓
              Token    Request sin modificar
                  ↓
      Añadir header: Authorization: Bearer {token}
                  ↓
              Request Autenticado
                  ↓
              Servidor
```

---

## 5. Capa de Dominio (Domain Layer)

### 5.1. Modelos de Dominio

**Archivo:** `model/SaborLocalModels.kt`

```kotlin
package com.example.miappmodular.model

/**
 * Modelo de dominio para Usuario
 */
data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val role: String,  // CLIENTE, PRODUCTOR, ADMIN
    val telefono: String? = null,
    val ubicacion: String? = null,
    val direccion: String? = null
) {
    fun isCliente(): Boolean = role == "CLIENTE"
    fun isProductor(): Boolean = role == "PRODUCTOR"
    fun isAdmin(): Boolean = role == "ADMIN"
}

/**
 * Modelo para Producto
 */
data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productorId: String,
    val productorNombre: String?,
    val imagen: String? = null
)

/**
 * Modelo para Productor (NO USADO - usamos Users con role PRODUCTOR)
 */
data class Productor(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String
)
```

**💡 DTO vs Modelo de Dominio:**

| DTO (Data Layer) | Modelo (Domain Layer) |
|------------------|----------------------|
| Refleja estructura JSON exacta | Estructura optimizada para la app |
| Usa `@SerializedName` | Sin anotaciones |
| Campos opcionales del backend | Solo lo que la app necesita |
| `UserDto` | `User` |

### 5.2. Repository Pattern

**Archivo:** `repository/AuthSaborLocalRepository.kt`

```kotlin
package com.example.miappmodular.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.*
import com.example.miappmodular.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository para autenticación
 *
 * Responsabilidades:
 * - Llamar a la API
 * - Guardar/recuperar token
 * - Convertir DTOs a Modelos
 * - Manejo de errores
 */
class AuthSaborLocalRepository(context: Context) {

    private val apiService = RetrofitClient.saborLocalApiService
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "saborlocal_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
    }

    /**
     * Login de usuario
     */
    suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val request = LoginSaborLocalRequest(email, password)
                val response = apiService.login(request)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.success && body.data != null) {
                        val authData = body.data

                        // Guardar sesión
                        saveSession(authData.accessToken, authData.user)

                        // Convertir DTO a Modelo
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
                nombre, email, password, telefono, direccion
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
     * Crear usuario PRODUCTOR (solo ADMIN)
     */
    suspend fun createProductorUser(
        nombre: String,
        email: String,
        password: String,
        ubicacion: String,
        telefono: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = CreateProductorUserRequest(
                nombre, email, password, ubicacion, telefono
            )
            val response = apiService.createProductorUser(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val authData = body.data
                    // NO guardamos sesión (el ADMIN sigue logueado)

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
                    403 -> "No tienes permisos (requiere rol ADMIN)"
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
     * Obtener todos los usuarios (solo ADMIN)
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
                    403 -> "No tienes permisos (requiere rol ADMIN)"
                    else -> "Error HTTP ${response.code()}"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.message}", e))
        }
    }

    /**
     * Guardar token y datos de usuario
     */
    private fun saveSession(token: String, user: UserDto) {
        prefs.edit().apply {
            putString(KEY_TOKEN, token)
            putString(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.nombre)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_ROLE, user.role)
            apply()
        }
    }

    /**
     * Verificar si hay sesión activa
     */
    fun isLoggedIn(): Boolean {
        return prefs.getString(KEY_TOKEN, null) != null
    }

    /**
     * Obtener usuario actual
     */
    fun getCurrentUser(): User? {
        val token = prefs.getString(KEY_TOKEN, null) ?: return null
        val userId = prefs.getString(KEY_USER_ID, null) ?: return null
        val userName = prefs.getString(KEY_USER_NAME, null) ?: return null
        val userEmail = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val userRole = prefs.getString(KEY_USER_ROLE, null) ?: return null

        return User(
            id = userId,
            nombre = userName,
            email = userEmail,
            role = userRole
        )
    }

    /**
     * Cerrar sesión
     */
    fun logout() {
        prefs.edit().clear().apply()
    }
}
```

**💡 Conceptos Clave del Repository:**

1. **Single Source of Truth**: El Repository decide de dónde vienen los datos
2. **Error Handling**: Traduce errores HTTP a mensajes amigables
3. **Conversión DTO → Model**: Separa capa de datos de capa de dominio
4. **withContext(Dispatchers.IO)**: Ejecuta en hilo de I/O (no bloquea UI)
5. **Result<T>**: Kotlin Result type para manejar success/failure

---

## 6. Capa de Presentación (Presentation Layer)

### 6.1. ViewModels

**Archivo:** `viewmodel/LoginViewModel.kt`

```kotlin
package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.User
import com.example.miappmodular.repository.AuthSaborLocalRepository
import com.example.miappmodular.utils.ValidationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para Login Screen
 *
 * Responsabilidades:
 * - Mantener estado de UI (email, password, errors)
 * - Validar inputs
 * - Llamar al Repository para login
 * - Exponer estados para la UI (Loading, Success, Error)
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository(application)

    // Estados de entrada
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Estados de validación
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // Estado de visibilidad de password
    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    // Estado de UI
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualizar email y validar
     */
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = ValidationUtils.validateEmail(newEmail)
    }

    /**
     * Actualizar password y validar
     */
    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = ValidationUtils.validatePassword(newPassword)
    }

    /**
     * Toggle visibilidad de password
     */
    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    /**
     * Ejecutar login
     */
    fun login() {
        // Validar campos
        val emailValidation = ValidationUtils.validateEmail(_email.value)
        val passwordValidation = ValidationUtils.validatePassword(_password.value)

        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        // Si hay errores, no continuar
        if (emailValidation != null || passwordValidation != null) {
            _uiState.value = LoginUiState.Error("Por favor corrige los errores")
            return
        }

        // Cambiar a estado Loading
        _uiState.value = LoginUiState.Loading

        // Llamar al Repository
        viewModelScope.launch {
            val result = repository.login(_email.value, _password.value)

            _uiState.value = result.fold(
                onSuccess = { user -> LoginUiState.Success(user) },
                onFailure = { error -> LoginUiState.Error(error.message ?: "Error desconocido") }
            )
        }
    }
}

/**
 * Estados de UI para Login
 */
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

**💡 Conceptos del ViewModel:**

1. **StateFlow**: Estado reactivo que la UI observa
2. **MutableStateFlow**: Privado en VM, solo el VM lo modifica
3. **asStateFlow()**: Expone versión inmutable a la UI
4. **viewModelScope.launch**: Coroutine que se cancela con el ViewModel
5. **Sealed Class**: Estados mutuamente excluyentes (Idle, Loading, Success, Error)

### 6.2. Validación

**Archivo:** `utils/ValidationUtils.kt`

```kotlin
package com.example.miappmodular.utils

object ValidationUtils {

    /**
     * Valida email
     * @return null si es válido, mensaje de error si no
     */
    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Email inválido"
            else -> null
        }
    }

    /**
     * Valida password
     * Mínimo 8 caracteres, al menos 1 mayúscula, 1 minúscula, 1 número
     */
    fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "La contraseña es obligatoria"
            password.length < 8 -> "Mínimo 8 caracteres"
            !password.any { it.isUpperCase() } -> "Debe tener al menos 1 mayúscula"
            !password.any { it.isLowerCase() } -> "Debe tener al menos 1 minúscula"
            !password.any { it.isDigit() } -> "Debe tener al menos 1 número"
            else -> null
        }
    }

    /**
     * Valida nombre
     */
    fun isValidName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 3 -> "El nombre debe tener al menos 3 caracteres"
            else -> null
        }
    }

    /**
     * Valida teléfono
     */
    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "El teléfono es obligatorio"
            else -> {
                val digitsOnly = phone.filter { it.isDigit() }
                when {
                    digitsOnly.length < 8 -> "Mínimo 8 dígitos"
                    digitsOnly.length > 15 -> "Máximo 15 dígitos"
                    !phone.all { it.isDigit() || it in setOf('+', '-', ' ', '(', ')') } ->
                        "Solo números y símbolos (+, -, espacios, paréntesis)"
                    else -> null
                }
            }
        }
    }
}
```

### 6.3. Screens (UI con Compose)

**Archivo:** `ui/screens/LoginScreen.kt`

```kotlin
package com.example.miappmodular.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.viewmodel.LoginViewModel
import com.example.miappmodular.viewmodel.LoginUiState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    // Observar estados del ViewModel
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Efecto secundario para navegar al login exitoso
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Título
        Icon(
            imageVector = Icons.Default.Agriculture,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SaborLocal",
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            text = "Inicia sesión",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null)
            },
            isError = emailError != null,
            supportingText = emailError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Password
        OutlinedTextField(
            value = password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Contraseña") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null)
            },
            trailingIcon = {
                IconButton(onClick = viewModel::togglePasswordVisibility) {
                    Icon(
                        imageVector = if (isPasswordVisible)
                            Icons.Default.VisibilityOff
                        else
                            Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible)
                            "Ocultar"
                        else
                            "Mostrar"
                    )
                }
            },
            visualTransformation = if (isPasswordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            isError = passwordError != null,
            supportingText = passwordError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Login
        Button(
            onClick = viewModel::login,
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Iniciar Sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de error
        if (uiState is LoginUiState.Error) {
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link a Registro
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
```

**💡 Conceptos de Compose:**

1. **@Composable**: Función que puede renderizar UI
2. **collectAsState()**: Convierte StateFlow a State (reactivo)
3. **LaunchedEffect**: Efecto secundario que se ejecuta cuando cambia una key
4. **remember**: Mantiene estado entre recomposiciones
5. **Modifier**: Encadena modificaciones de UI (padding, size, etc.)

---

## 7. Sistema de Navegación

**Archivo:** `ui/navigation/AppNavigation.kt`

```kotlin
package com.example.miappmodular.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miappmodular.ui.screens.*

/**
 * Grafo de navegación de la app
 *
 * Rutas:
 * - splash → Verifica sesión
 * - login → Login
 * - register → Registro
 * - home → Dashboard
 * - profile → Perfil
 * - productos_list → Lista productos
 * - create_producto → Crear producto
 * - create_productor → Crear productor (ADMIN)
 * - productores_list → Lista productores
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Splash - Verifica sesión
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Login
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Register
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigateUp()
                },
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable("home") {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate("profile")
                },
                onNavigateToProductosList = {
                    navController.navigate("productos_list")
                },
                onNavigateToCreateProducto = {
                    navController.navigate("create_producto")
                },
                onNavigateToCreateProductor = {
                    navController.navigate("create_productor")
                },
                onNavigateToProductoresList = {
                    navController.navigate("productores_list")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        // Profile
        composable("profile") {
            ProfileScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }

        // Lista Productos
        composable("productos_list") {
            ProductosListScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductClick = { productId ->
                    // TODO: Detalle de producto
                }
            )
        }

        // Crear Producto
        composable("create_producto") {
            CreateProductoScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductoCreated = {
                    navController.popBackStack("productos_list", inclusive = false)
                }
            )
        }

        // Crear Productor (ADMIN)
        composable("create_productor") {
            CreateProductorScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductorCreated = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }

        // Lista Productores
        composable("productores_list") {
            ProductoresListScreen(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onProductorClick = { productorId ->
                    // TODO: Detalle de productor
                }
            )
        }
    }
}
```

**💡 Conceptos de Navegación:**

1. **NavHost**: Contenedor de rutas
2. **composable("route")**: Define una pantalla
3. **navigate("route")**: Ir a una ruta
4. **navigateUp()**: Volver atrás
5. **popUpTo**: Limpiar backstack hasta una ruta
6. **inclusive = true**: Incluir la ruta en la limpieza

**Flujo de Backstack:**

```
Splash → Login → Home
  ↓        ↓       ↓
Limpia  Limpia  Backstack
 todo    todo    vacío

Login → Register → [Atrás] → Login
  ↓                           ↓
[Registro OK] → Home
                ↓
            Limpia hasta Login
```

---

## 8. Autenticación y Sesión

### 8.1. SplashScreen (Auto-login)

**Archivo:** `ui/screens/SplashScreen.kt`

```kotlin
package com.example.miappmodular.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.viewmodel.SplashViewModel
import com.example.miappmodular.viewmodel.SplashNavigationState

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = viewModel(),
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val navigationState by viewModel.navigationState.collectAsState()

    // Navegar según el estado
    LaunchedEffect(navigationState) {
        when (navigationState) {
            SplashNavigationState.NavigateToHome -> onNavigateToHome()
            SplashNavigationState.NavigateToLogin -> onNavigateToLogin()
            SplashNavigationState.Loading -> { /* Mostrar splash */ }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
```

**ViewModel:**

```kotlin
package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.repository.AuthSaborLocalRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository(application)

    private val _navigationState = MutableStateFlow<SplashNavigationState>(
        SplashNavigationState.Loading
    )
    val navigationState: StateFlow<SplashNavigationState> = _navigationState

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            delay(1000) // Simular splash

            if (repository.isLoggedIn()) {
                val currentUser = repository.getCurrentUser()
                if (currentUser != null) {
                    _navigationState.value = SplashNavigationState.NavigateToHome
                } else {
                    _navigationState.value = SplashNavigationState.NavigateToLogin
                }
            } else {
                _navigationState.value = SplashNavigationState.NavigateToLogin
            }
        }
    }
}

sealed class SplashNavigationState {
    object Loading : SplashNavigationState()
    object NavigateToHome : SplashNavigationState()
    object NavigateToLogin : SplashNavigationState()
}
```

### 8.2. SharedPreferences vs DataStore

| SharedPreferences | DataStore |
|-------------------|-----------|
| Sincrónico (bloquea) | Asíncrono (coroutines) |
| XML | Protocol Buffers |
| Puede corromper datos | Transaccional |
| Simple | Moderno |
| `saborlocal_prefs` (SaborLocal) | `SessionManager` (Xano) |

**En nuestro proyecto:**
- **SaborLocal API**: Usa SharedPreferences (`saborlocal_prefs`)
- **Xano API (vieja)**: Usa DataStore (`SessionManager`)
- **AuthInterceptor**: Lee de ambas (prioridad: SaborLocal)

---

## 9. Gestión de Productos y Productores

### 9.1. Crear Productor (Solo ADMIN)

**Flujo:**

```
ADMIN logueado
    ↓
HomeScreen → "Crear Productor"
    ↓
CreateProductorScreen
    ↓
Formulario: nombre, email, password, ubicación, teléfono
    ↓
Validaciones en tiempo real
    ↓
POST /api/auth/create-productor
    ↓
Usuario creado con role="PRODUCTOR"
    ↓
Mensaje de éxito + scroll automático
    ↓
ADMIN sigue logueado (no se cambia sesión)
```

**Características:**

✅ Validación en tiempo real
✅ Autocomplete de ubicación (regiones de Chile)
✅ Visibilidad de password toggle
✅ Mensaje de éxito animado
✅ Scroll automático al éxito

### 9.2. Listar Productores

**Cambio importante:**

Antes:
```kotlin
// ❌ Traía de tabla "productores" (separada)
repository.getProductores() // GET /api/productor
```

Ahora:
```kotlin
// ✅ Trae de tabla "users" filtrado por role
repository.getAllUsers()
  .filter { it.isProductor() }
```

**¿Por qué?**

Los productores son **Usuarios con role="PRODUCTOR"**, no una entidad separada. Esto permite:
- ✅ Los productores pueden iniciar sesión
- ✅ Un solo sistema de usuarios
- ✅ Sin duplicación de datos
- ✅ Autenticación integrada

### 9.3. Crear Producto (Solo PRODUCTOR)

**Flujo:**

```
PRODUCTOR logueado
    ↓
HomeScreen → "Crear Producto"
    ↓
CreateProductoScreen
    ↓
Formulario: nombre, descripción, precio, unidad, stock, productor
    ↓
POST /api/producto
    ↓
Producto creado asociado al productor
```

### 9.4. Listar Productos con Filtros

**Características:**

- 🔍 Búsqueda por nombre/descripción
- 💰 Filtro por rango de precios
- 👨‍🌾 Filtro por productor
- 📊 Ordenamiento (precio, nombre)

---

## 10. Características Avanzadas

### 10.1. Interceptor JWT

**¿Qué hace?**

Añade automáticamente el token JWT a **TODAS** las peticiones HTTP:

```
Request sin interceptor:
GET /api/producto

Request con interceptor:
GET /api/producto
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Beneficios:**

- ✅ No tienes que añadir el token manualmente en cada llamada
- ✅ Centralizado en un solo lugar
- ✅ Funciona con múltiples fuentes de tokens (SharedPreferences + DataStore)

### 10.2. Logging Interceptor

**¿Qué hace?**

Muestra en Logcat todas las peticiones y respuestas:

```
--> POST /api/auth/login
Content-Type: application/json
{
  "email": "test@example.com",
  "password": "password123"
}
--> END POST

<-- 200 OK /api/auth/login (234ms)
Content-Type: application/json
{
  "success": true,
  "data": {
    "user": { ... },
    "access_token": "..."
  }
}
<-- END HTTP
```

**⚠️ En producción:**
```kotlin
level = HttpLoggingInterceptor.Level.NONE // Desactivar
```

### 10.3. Validación en Tiempo Real

```kotlin
// Usuario escribe en campo email
onEmailChange("test@")
    ↓
viewModel.onEmailChange("test@")
    ↓
_email.value = "test@"
_emailError.value = ValidationUtils.validateEmail("test@")
    ↓
_emailError.value = "Email inválido"
    ↓
UI observa emailError StateFlow
    ↓
OutlinedTextField muestra error en rojo
```

### 10.4. Autocomplete de Ubicación

```kotlin
val regionesChile = listOf(
    "Arica y Parinacota",
    "Tarapacá",
    "Antofagasta",
    // ...
    "Magallanes y la Antártica Chilena"
)

ExposedDropdownMenuBox(
    expanded = ubicacionExpanded && regionesFiltradas.isNotEmpty(),
    onExpandedChange = { ubicacionExpanded = !ubicacionExpanded }
) {
    OutlinedTextField(
        value = ubicacion,
        onValueChange = {
            viewModel.onUbicacionChange(it)
            ubicacionExpanded = true
        }
    )

    ExposedDropdownMenu(...) {
        regionesFiltradas.forEach { region ->
            DropdownMenuItem(
                text = { Text(region) },
                onClick = {
                    viewModel.onUbicacionChange(region)
                    ubicacionExpanded = false
                }
            )
        }
    }
}
```

---

## 11. Buenas Prácticas Implementadas

### 11.1. Arquitectura MVVM

✅ **Separación de responsabilidades**
- View: Solo UI, no lógica de negocio
- ViewModel: Estado y lógica de presentación
- Repository: Acceso a datos

✅ **Testeable**
- ViewModels sin dependencias de Android
- Repositories con interfaces

### 11.2. Manejo de Errores

```kotlin
// ✅ Traducir errores HTTP a mensajes amigables
val errorMessage = when (response.code()) {
    401 -> "Credenciales inválidas"
    404 -> "Usuario no encontrado"
    409 -> "El email ya está registrado"
    else -> "Error HTTP ${response.code()}"
}
```

### 11.3. Estados de UI con Sealed Class

```kotlin
sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

**Ventajas:**

- ✅ Mutuamente excluyentes (solo un estado a la vez)
- ✅ Type-safe (el compilador verifica todos los casos)
- ✅ Exhaustive when (no puedes olvidar un caso)

### 11.4. Coroutines

```kotlin
// ✅ Ejecutar en hilo correcto
viewModelScope.launch {  // Main thread
    val result = withContext(Dispatchers.IO) {  // I/O thread
        repository.login(email, password)
    }
    _uiState.value = result  // Vuelve a Main thread
}
```

### 11.5. StateFlow vs LiveData

| StateFlow | LiveData |
|-----------|----------|
| Kotlin Flow | Android-specific |
| Requiere valor inicial | Puede empezar null |
| Más moderno | Legacy |
| Funciona fuera de Android | Solo Android |
| `collectAsState()` en Compose | Requiere observers |

### 11.6. Singleton Pattern

```kotlin
object RetrofitClient {  // ✅ Solo una instancia global
    private val retrofit: Retrofit by lazy { ... }
}
```

### 11.7. Dependency Injection Manual

```kotlin
class LoginViewModel(application: Application) {
    private val repository = AuthSaborLocalRepository(application)
}
```

**En proyectos grandes:**
- Usar Hilt o Koin para DI automático

### 11.8. Network Security

```xml
<!-- ✅ Desarrollo -->
<domain-config cleartextTrafficPermitted="true">
    <domain>10.0.2.2</domain>
</domain-config>

<!-- ✅ Producción -->
<base-config cleartextTrafficPermitted="false">
    <trust-anchors>
        <certificates src="system" />
    </trust-anchors>
</base-config>
```

---

## 12. Resumen del Flujo Completo

### Flujo de Registro

```
1. Usuario abre app → SplashScreen
2. No hay token → LoginScreen
3. Click "Regístrate" → RegisterScreen
4. Llena formulario:
   - Nombre: "Juan Pérez"
   - Email: "juan@example.com"
   - Password: "Password123"
5. RegisterViewModel valida inputs
6. Si válido → RegisterViewModel.register()
7. Repository.register() → POST /api/auth/register
8. Backend crea usuario con role="CLIENTE"
9. Backend retorna { user, access_token }
10. Repository guarda token en SharedPreferences
11. Repository convierte UserDto → User
12. ViewModel cambia estado a Success(user)
13. Screen observa Success → navega a HomeScreen
14. Usuario ve dashboard como CLIENTE
```

### Flujo de Login

```
1. Usuario abre app → SplashScreen
2. Ya hay token → SplashViewModel.checkSession()
3. Token válido → Navega a HomeScreen directamente
4. Usuario ve dashboard sin login
```

### Flujo de Crear Productor (ADMIN)

```
1. ADMIN logueado → HomeScreen
2. Click "Crear Productor" → CreateProductorScreen
3. Llena formulario:
   - Nombre: "Agricola Pérez"
   - Email: "productor@example.com"
   - Password: "Password123"
   - Ubicación: "Valparaíso" (autocomplete)
   - Teléfono: "987654321"
4. CreateProductorViewModel valida
5. Si válido → CreateProductorViewModel.createProductor()
6. Repository.createProductorUser() → POST /api/auth/create-productor
7. AuthInterceptor añade: Authorization: Bearer {admin_token}
8. Backend verifica role="ADMIN"
9. Backend crea usuario con role="PRODUCTOR"
10. Backend retorna { user, access_token } (del nuevo productor)
11. Repository NO guarda sesión (ADMIN sigue logueado)
12. ViewModel cambia estado a Success
13. Screen muestra mensaje de éxito
14. Screen hace scroll automático al mensaje
15. ADMIN puede crear otro productor
```

### Flujo de Listar Productores

```
1. Usuario (cualquier rol) → HomeScreen
2. Click "Ver Productores" → ProductoresListScreen
3. ProductoresListViewModel.loadProductores()
4. Repository.getAllUsers() → GET /api/auth/users
5. AuthInterceptor añade token JWT
6. Backend verifica token y rol ADMIN (solo ADMIN puede ver todos)
7. Backend retorna lista de usuarios
8. Repository filtra: users.filter { it.isProductor() }
9. ViewModel cambia estado a Success(productores)
10. Screen muestra lista con:
    - Nombre
    - Ubicación
    - Teléfono
    - Email
11. Usuario puede buscar por nombre/ubicación/email
```

---

## 13. Diagramas

### Diagrama de Arquitectura

```
┌─────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                  │
│                                                      │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────┐ │
│  │ LoginScreen  │  │ HomeScreen   │  │ Profile   │ │
│  └──────┬───────┘  └──────┬───────┘  └─────┬─────┘ │
│         │                 │                 │       │
│         └─────────┬───────┴─────────────────┘       │
│                   ↓                                  │
│  ┌─────────────────────────────────────────────┐   │
│  │         LoginViewModel                      │   │
│  │  - email: StateFlow<String>                │   │
│  │  - password: StateFlow<String>              │   │
│  │  - uiState: StateFlow<LoginUiState>        │   │
│  │  + login()                                  │   │
│  └─────────────────┬───────────────────────────┘   │
└────────────────────┼───────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│                   DOMAIN LAYER                       │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │    AuthSaborLocalRepository                 │   │
│  │  + login(email, password): Result<User>     │   │
│  │  + register(...): Result<User>              │   │
│  │  + isLoggedIn(): Boolean                    │   │
│  │  + getCurrentUser(): User?                  │   │
│  └─────────────────┬───────────────────────────┘   │
└────────────────────┼───────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────┐
│                    DATA LAYER                        │
│                                                      │
│  ┌─────────────────────────────────────────────┐   │
│  │     SaborLocalApiService (Retrofit)         │   │
│  │  suspend fun login(): Response<...>         │   │
│  └─────────────────┬───────────────────────────┘   │
│                    │                                │
│  ┌─────────────────┴───────────────────────────┐   │
│  │           RetrofitClient                    │   │
│  │  - OkHttpClient (+ Interceptors)            │   │
│  │  - Retrofit                                 │   │
│  └─────────────────┬───────────────────────────┘   │
│                    │                                │
│  ┌─────────────────┴───────────────────────────┐   │
│  │         AuthInterceptor                     │   │
│  │  - Añade header: Authorization: Bearer      │   │
│  └─────────────────┬───────────────────────────┘   │
└────────────────────┼───────────────────────────────┘
                     ↓
              ┌──────────────┐
              │   Backend    │
              │  (Node.js)   │
              └──────────────┘
```

### Diagrama de Flujo de Datos

```
User Input (UI)
    ↓
ViewModel (validate + update StateFlow)
    ↓
Repository (API call + error handling)
    ↓
API Service (Retrofit)
    ↓
OkHttp (+ Interceptors: Auth, Logging)
    ↓
Backend Server
    ↓
Response (JSON)
    ↓
Gson (JSON → DTO)
    ↓
Repository (DTO → Model + save session)
    ↓
ViewModel (update uiState)
    ↓
UI (observe StateFlow → recompose)
    ↓
User sees result
```

---

## 14. Comandos Útiles

### Compilar Proyecto

```bash
# En terminal de Android Studio
./gradlew build

# Solo compilar sin tests
./gradlew assembleDebug

# Limpiar y compilar
./gradlew clean build
```

### Ver Logs

```bash
# Filtrar por tag
adb logcat -s "OkHttp"

# Ver solo errores
adb logcat *:E

# Limpiar logs
adb logcat -c
```

### Instalar APK

```bash
# Debug APK
./gradlew installDebug

# Release APK
./gradlew installRelease
```

---

## 15. Checklist de Implementación

### Fase 1: Configuración ✅
- [x] Crear proyecto Android
- [x] Agregar dependencias
- [x] Configurar permisos
- [x] Network security config
- [x] Estructura de carpetas

### Fase 2: Data Layer ✅
- [x] DTOs
- [x] API Service (Retrofit)
- [x] RetrofitClient (Singleton)
- [x] AuthInterceptor
- [x] SessionManager/SharedPreferences

### Fase 3: Domain Layer ✅
- [x] Modelos de dominio
- [x] Repository (Auth)
- [x] Repository (Productos)
- [x] Repository (Productores → Users)
- [x] ValidationUtils

### Fase 4: Presentation Layer ✅
- [x] ViewModels (Login, Register, Home, etc.)
- [x] Screens (Login, Register, Home, etc.)
- [x] Navegación
- [x] Tema shadcn
- [x] Componentes reutilizables

### Fase 5: Features Avanzadas ✅
- [x] SplashScreen (auto-login)
- [x] Crear Productor (ADMIN)
- [x] Listar Productores (filtrado de Users)
- [x] Crear Producto (PRODUCTOR)
- [x] Listar Productos (con filtros)
- [x] Validación en tiempo real
- [x] Autocomplete ubicación

### Fase 6: Testing ⬜ (Siguiente paso)
- [ ] Unit tests (Repository)
- [ ] Unit tests (ViewModel)
- [ ] UI tests (Screens)
- [ ] Integration tests

---

## 16. Próximos Pasos

1. **Testing** → Crear pruebas unitarias (ver TESTING_GUIDE.md)
2. **Detalles de Producto** → Pantalla para ver producto completo
3. **Editar Producto** → PATCH /api/producto/{id}
4. **Carrito de Compras** → CLIENTES pueden crear pedidos
5. **Pedidos** → Ver historial de pedidos
6. **Notificaciones Push** → Firebase Cloud Messaging
7. **Modo Offline** → Room para caché local
8. **CI/CD** → GitHub Actions para builds automáticos

---

## 17. Recursos de Aprendizaje

### Documentación Oficial
- [Android Developers](https://developer.android.com/)
- [Kotlin Lang](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Retrofit](https://square.github.io/retrofit/)

### Tutoriales Recomendados
- [Codelabs Android](https://developer.android.com/codelabs)
- [Philipp Lackner YouTube](https://www.youtube.com/@PhilippLackner)
- [Android Developers YouTube](https://www.youtube.com/@AndroidDevelopers)

### Libros
- "Kotlin for Android Developers" - Antonio Leiva
- "Android Development with Kotlin" - Marcin Moskala

---

## 18. Créditos

**Profesor:** Roberto
**Estudiantes:** [Tus estudiantes]
**Curso:** Desarrollo de Aplicaciones Android
**Institución:** [Tu institución]
**Fecha:** 2025

---

**¡Feliz Codificación! 🚀📱**

---

## Anexo: Glosario de Términos

| Término | Significado |
|---------|-------------|
| **MVVM** | Model-View-ViewModel (patrón arquitectónico) |
| **DTO** | Data Transfer Object |
| **Repository** | Capa que abstrae el acceso a datos |
| **ViewModel** | Maneja estado y lógica de presentación |
| **StateFlow** | Estado reactivo en Kotlin |
| **Coroutine** | Hilo ligero para programación asíncrona |
| **Retrofit** | Cliente HTTP type-safe |
| **OkHttp** | Cliente HTTP de bajo nivel |
| **Interceptor** | Modifica requests/responses HTTP |
| **JWT** | JSON Web Token (autenticación) |
| **Sealed Class** | Clase con subclases limitadas |
| **Composable** | Función que renderiza UI en Compose |
| **LaunchedEffect** | Efecto secundario en Compose |
| **NavHost** | Contenedor de rutas de navegación |

---

**Fin del Tutorial** 🎓
