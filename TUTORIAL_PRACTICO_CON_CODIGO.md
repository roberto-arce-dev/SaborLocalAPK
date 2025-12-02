# Tutorial Práctico - Construye la App Paso a Paso
## De cero a una App Android Completa con Kotlin y Jetpack Compose

**Duración estimada:** 8-10 horas
**Nivel:** Intermedio

---

## 📚 Lo que vas a construir

Una aplicación de marketplace agrícola con:
- ✅ Sistema de login y registro
- ✅ 3 roles de usuario (CLIENTE, PRODUCTOR, ADMIN)
- ✅ Gestión de productos
- ✅ Gestión de productores
- ✅ Arquitectura MVVM profesional

---

## PASO 1: Configuración Inicial (15 min)

### 1.1. Crear el Proyecto

1. Abre Android Studio
2. **File → New → New Project**
3. Selecciona **Empty Activity**
4. Configura:
   ```
   Name: MiAppModular
   Package name: com.example.miappmodular
   Language: Kotlin
   Minimum SDK: API 24 (Android 7.0)
   Build configuration language: Kotlin DSL
   ```

### 1.2. Agregar Dependencias

Abre `app/build.gradle.kts` y reemplaza todo el contenido con:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.miappmodular"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.miappmodular"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

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

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
```

**Sincroniza el proyecto:** Click en "Sync Now"

### 1.3. Configurar Permisos de Internet

Abre `app/src/main/AndroidManifest.xml` y agrega los permisos:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- ✅ AGREGAR ESTOS PERMISOS -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".MyApplication"
        android:networkSecurityConfig="@xml/network_security_config"
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MiAppModular"
        tools:targetApi="31">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MiAppModular">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 1.4. Crear Network Security Config

**Paso 1:** Crea la carpeta `xml` en `app/src/main/res/`
- Click derecho en `res/` → New → Android Resource Directory
- Resource type: `xml`
- Click OK

**Paso 2:** Crea el archivo `network_security_config.xml`
- Click derecho en `res/xml/` → New → File
- Nombre: `network_security_config.xml`

**Paso 3:** Pega este contenido:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Permitir HTTP solo para localhost (desarrollo) -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">localhost</domain>
        <domain includeSubdomains="true">127.0.0.1</domain>
    </domain-config>

    <!-- Producción: solo HTTPS -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

### 1.5. Crear Application Class

**Paso 1:** Click derecho en `com.example.miappmodular` → New → Kotlin Class/File
- Nombre: `MyApplication`

**Paso 2:** Escribe:

```kotlin
package com.example.miappmodular

import android.app.Application
import com.example.miappmodular.data.remote.RetrofitClient

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar Retrofit
        RetrofitClient.initialize(this)
    }
}
```

---

## PASO 2: Crear Estructura de Carpetas (10 min)

Crea esta estructura de carpetas haciendo click derecho en `com.example.miappmodular`:

```
com.example.miappmodular/
├── data/
│   ├── local/
│   └── remote/
│       └── dto/
├── model/
├── repository/
├── viewmodel/
├── ui/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   └── theme/
└── utils/
```

**Cómo crear cada carpeta:**
1. Click derecho en `com.example.miappmodular`
2. New → Package
3. Escribe el nombre (ej: `data.local`)

---

## PASO 3: DTOs - Modelos de la API (20 min)

Crea el archivo `SaborLocalDtos.kt` en `data/remote/dto/`:

```kotlin
package com.example.miappmodular.data.remote.dto

import com.google.gson.annotations.SerializedName

// ==================== WRAPPERS ====================

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null
)

// ==================== USER ====================

data class UserDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val email: String,
    val role: String,
    val telefono: String? = null,
    val ubicacion: String? = null,
    val direccion: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class AuthSaborLocalData(
    val user: UserDto,
    @SerializedName("access_token")
    val accessToken: String
)

// ==================== REQUESTS ====================

data class LoginSaborLocalRequest(
    val email: String,
    val password: String
)

data class RegisterSaborLocalRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null
)

data class CreateProductorUserRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val ubicacion: String,
    val telefono: String
)

// ==================== PRODUCTO ====================

data class ProductoDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: Any,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

data class CreateProductoRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: String
)

// ==================== PRODUCTOR ====================

data class ProductorDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String
)
```

---

## PASO 4: API Service - Definir Endpoints (15 min)

Crea `SaborLocalApiService.kt` en `data/remote/`:

```kotlin
package com.example.miappmodular.data.remote

import com.example.miappmodular.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface SaborLocalApiService {

    // ==================== AUTH ====================

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginSaborLocalRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterSaborLocalRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    @GET("auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    @GET("auth/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserDto>>>

    @POST("auth/create-productor")
    suspend fun createProductorUser(
        @Body request: CreateProductorUserRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    // ==================== PRODUCTOS ====================

    @GET("producto")
    suspend fun getProductos(): Response<ApiResponse<List<ProductoDto>>>

    @POST("producto")
    suspend fun createProducto(
        @Body request: CreateProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    @DELETE("producto/{id}")
    suspend fun deleteProducto(
        @Path("id") id: String
    ): Response<ApiResponse<Unit>>
}
```

---

## PASO 5: SessionManager (15 min)

Crea `SessionManager.kt` en `data/local/`:

```kotlin
package com.example.miappmodular.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

class SessionManager(private val context: Context) {

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun getAuthToken(): String? {
        return context.dataStore.data.map { preferences ->
            preferences[AUTH_TOKEN_KEY]
        }.first()
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
```

---

## PASO 6: Auth Interceptor (20 min)

Crea `AuthInterceptor.kt` en `data/remote/`:

```kotlin
package com.example.miappmodular.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.example.miappmodular.data.local.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager,
    private val context: Context
) : Interceptor {

    private val saborLocalPrefs: SharedPreferences by lazy {
        context.getSharedPreferences("saborlocal_prefs", Context.MODE_PRIVATE)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 1. Intentar token de SaborLocal
        var token = saborLocalPrefs.getString("auth_token", null)

        // 2. Fallback a SessionManager
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

---

## PASO 7: Retrofit Client (20 min)

Crea `RetrofitClient.kt` en `data/remote/`:

```kotlin
package com.example.miappmodular.data.remote

import android.content.Context
import com.example.miappmodular.data.local.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // ⚠️ IMPORTANTE: Cambia esta URL por la de tu backend
    private const val BASE_URL = "http://10.0.2.2:3008/api/"

    private lateinit var sessionManager: SessionManager
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context.applicationContext
        sessionManager = SessionManager(context.applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthInterceptor(sessionManager, context)

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

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val saborLocalApiService: SaborLocalApiService by lazy {
        retrofit.create(SaborLocalApiService::class.java)
    }
}
```

---

## PASO 8: Modelos de Dominio (10 min)

Crea `SaborLocalModels.kt` en `model/`:

```kotlin
package com.example.miappmodular.model

data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val role: String,
    val telefono: String? = null,
    val ubicacion: String? = null,
    val direccion: String? = null
) {
    fun isCliente(): Boolean = role == "CLIENTE"
    fun isProductor(): Boolean = role == "PRODUCTOR"
    fun isAdmin(): Boolean = role == "ADMIN"
}

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

data class Productor(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String
)
```

---

## PASO 9: Utilidades de Validación (15 min)

Crea `ValidationUtils.kt` en `utils/`:

```kotlin
package com.example.miappmodular.utils

object ValidationUtils {

    fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "El email es obligatorio"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Email inválido"
            else -> null
        }
    }

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

    fun isValidName(name: String): String? {
        return when {
            name.isBlank() -> "El nombre es obligatorio"
            name.length < 3 -> "Mínimo 3 caracteres"
            else -> null
        }
    }

    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "El teléfono es obligatorio"
            else -> {
                val digitsOnly = phone.filter { it.isDigit() }
                when {
                    digitsOnly.length < 8 -> "Mínimo 8 dígitos"
                    digitsOnly.length > 15 -> "Máximo 15 dígitos"
                    !phone.all { it.isDigit() || it in setOf('+', '-', ' ', '(', ')') } ->
                        "Solo números y símbolos permitidos"
                    else -> null
                }
            }
        }
    }
}
```

---

## PASO 10: Repository de Autenticación (30 min)

Crea `AuthSaborLocalRepository.kt` en `repository/`:

```kotlin
package com.example.miappmodular.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.*
import com.example.miappmodular.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // ==================== LOGIN ====================

    suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
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

    // ==================== REGISTER ====================

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

    // ==================== SESSION ====================

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

    fun isLoggedIn(): Boolean {
        return prefs.getString(KEY_TOKEN, null) != null
    }

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

    fun logout() {
        prefs.edit().clear().apply()
    }
}
```

---

## PASO 11: ViewModel de Login (25 min)

Crea `LoginViewModel.kt` en `viewmodel/`:

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

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository(application)

    // Estados de entrada
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    // Estados de error
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    // Visibilidad de password
    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    // Estado de UI
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = ValidationUtils.validateEmail(newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = ValidationUtils.validatePassword(newPassword)
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun login() {
        // Validar
        val emailValidation = ValidationUtils.validateEmail(_email.value)
        val passwordValidation = ValidationUtils.validatePassword(_password.value)

        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        if (emailValidation != null || passwordValidation != null) {
            _uiState.value = LoginUiState.Error("Corrige los errores")
            return
        }

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = repository.login(_email.value, _password.value)

            _uiState.value = result.fold(
                onSuccess = { user -> LoginUiState.Success(user) },
                onFailure = { error ->
                    LoginUiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

---

## PASO 12: LoginScreen UI (30 min)

Crea `LoginScreen.kt` en `ui/screens/`:

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
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

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
        // Logo
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
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Email
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

        // Password
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
                        contentDescription = null
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

        // Error
        if (uiState is LoginUiState.Error) {
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link Registro
        TextButton(onClick = onNavigateToRegister) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
```

---

## PASO 13: ViewModel de Registro (20 min)

Crea `RegisterViewModel.kt` en `viewmodel/`:

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

class RegisterViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthSaborLocalRepository(application)

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _nombreError = MutableStateFlow<String?>(null)
    val nombreError: StateFlow<String?> = _nombreError.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _isPasswordVisible = MutableStateFlow(false)
    val isPasswordVisible: StateFlow<Boolean> = _isPasswordVisible.asStateFlow()

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombreChange(newNombre: String) {
        _nombre.value = newNombre
        _nombreError.value = ValidationUtils.isValidName(newNombre)
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        _emailError.value = ValidationUtils.validateEmail(newEmail)
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        _passwordError.value = ValidationUtils.validatePassword(newPassword)
    }

    fun togglePasswordVisibility() {
        _isPasswordVisible.value = !_isPasswordVisible.value
    }

    fun register() {
        val nombreValidation = ValidationUtils.isValidName(_nombre.value)
        val emailValidation = ValidationUtils.validateEmail(_email.value)
        val passwordValidation = ValidationUtils.validatePassword(_password.value)

        _nombreError.value = nombreValidation
        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        if (nombreValidation != null || emailValidation != null || passwordValidation != null) {
            _uiState.value = RegisterUiState.Error("Corrige los errores")
            return
        }

        _uiState.value = RegisterUiState.Loading

        viewModelScope.launch {
            val result = repository.register(
                nombre = _nombre.value,
                email = _email.value,
                password = _password.value
            )

            _uiState.value = result.fold(
                onSuccess = { user -> RegisterUiState.Success(user) },
                onFailure = { error ->
                    RegisterUiState.Error(error.message ?: "Error desconocido")
                }
            )
        }
    }
}

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success(val user: User) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}
```

---

## PASO 14: RegisterScreen UI (25 min)

Crea `RegisterScreen.kt` en `ui/screens/`:

```kotlin
package com.example.miappmodular.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.miappmodular.viewmodel.RegisterViewModel
import com.example.miappmodular.viewmodel.RegisterUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val nombre by viewModel.nombre.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val nombreError by viewModel.nombreError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val isPasswordVisible by viewModel.isPasswordVisible.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre completo") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                isError = nombreError != null,
                supportingText = nombreError?.let { { Text(it) } },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email
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

            // Password
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
                            contentDescription = null
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

            // Botón Registro
            Button(
                onClick = viewModel::register,
                enabled = uiState !is RegisterUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState is RegisterUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Crear Cuenta")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error
            if (uiState is RegisterUiState.Error) {
                Text(
                    text = (uiState as RegisterUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Link Login
            TextButton(onClick = onNavigateToLogin) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
```

---

## PASO 15: HomeScreen Simple (20 min)

Crea `HomeScreen.kt` en `ui/screens/`:

```kotlin
package com.example.miappmodular.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SaborLocal - Home") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "¡Bienvenido!",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Has iniciado sesión exitosamente",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
```

---

## PASO 16: Navegación (25 min)

Crea `AppNavigation.kt` en `ui/navigation/`:

```kotlin
package com.example.miappmodular.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miappmodular.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
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
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
```

---

## PASO 17: MainActivity (10 min)

Abre `MainActivity.kt` y reemplaza con:

```kotlin
package com.example.miappmodular

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.miappmodular.ui.navigation.AppNavigation
import com.example.miappmodular.ui.theme.MiAppModularTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MiAppModularTheme {
                AppNavigation()
            }
        }
    }
}
```

---

## PASO 18: Ejecutar la App (5 min)

### Opción A: Emulador

1. En Android Studio: **Tools → Device Manager**
2. Click "Create Device"
3. Selecciona Pixel 6
4. System Image: API 34
5. Finish
6. Click ▶️ (Run 'app')

### Opción B: Dispositivo Físico

1. Habilita "Opciones de desarrollador" en tu Android
2. Activa "Depuración USB"
3. Conecta por USB
4. Click ▶️ (Run 'app')

---

## PASO 19: Probar la App

### Test 1: Registro

1. Click "¿No tienes cuenta? Regístrate"
2. Llena:
   ```
   Nombre: Juan Pérez
   Email: juan@example.com
   Password: Password123
   ```
3. Click "Crear Cuenta"
4. Deberías ver HomeScreen

### Test 2: Logout y Login

1. Click icono de Logout
2. Vuelves a LoginScreen
3. Inicia sesión:
   ```
   Email: juan@example.com
   Password: Password123
   ```
4. Click "Iniciar Sesión"
5. Deberías ver HomeScreen

### Test 3: Validaciones

1. En Login, deja email vacío
2. Click "Iniciar Sesión"
3. Deberías ver error: "El email es obligatorio"

4. Escribe email inválido: "test"
5. Deberías ver error: "Email inválido"

6. Password corto: "abc"
7. Deberías ver error: "Mínimo 8 caracteres"

---

## PASO 20: Ver Logs de Red

1. Abre **Logcat** en Android Studio
2. Filtra por "OkHttp"
3. Intenta hacer login
4. Deberías ver:

```
--> POST http://10.0.2.2:3008/api/auth/login
Content-Type: application/json
{
  "email": "juan@example.com",
  "password": "Password123"
}
--> END POST

<-- 200 OK http://10.0.2.2:3008/api/auth/login (234ms)
{
  "success": true,
  "data": {
    "user": { ... },
    "access_token": "..."
  }
}
<-- END HTTP
```

---

## Troubleshooting (Solución de Problemas)

### Error: "CLEARTEXT communication not permitted"

**Solución:** Verifica que tengas el archivo `network_security_config.xml` correcto.

### Error: "Unable to resolve host"

**Solución:**
1. Verifica que el backend esté corriendo en `http://localhost:3008`
2. En emulador, usa `10.0.2.2` en lugar de `localhost`
3. En dispositivo físico, usa la IP de tu PC (ej: `192.168.1.10`)

### Error: "Unresolved reference"

**Solución:**
1. **Build → Clean Project**
2. **Build → Rebuild Project**
3. Sync Gradle

### Error: Imports en rojo

**Solución:**
1. Verifica que el package sea correcto: `com.example.miappmodular`
2. **File → Invalidate Caches → Invalidate and Restart**

---

## ¡Felicidades! 🎉

Has construido una app Android completa con:

✅ Arquitectura MVVM
✅ Retrofit para API
✅ Jetpack Compose UI
✅ Navegación
✅ Autenticación JWT
✅ Validaciones
✅ Manejo de estados
✅ Persistencia local

---

## Próximos Pasos

Ahora que tienes la base funcional, puedes agregar:

1. **SplashScreen** - Auto-login al abrir la app
2. **ProfileScreen** - Ver datos del usuario
3. **Lista de Productos** - Mostrar productos del backend
4. **Crear Producto** - Formulario para PRODUCTORES
5. **Filtros de búsqueda** - Buscar productos
6. **Imágenes** - Subir fotos de productos con Coil
7. **Tests Unitarios** - Probar ViewModels y Repositories

---

## Recursos de Ayuda

- **Android Developers:** https://developer.android.com/
- **Kotlin:** https://kotlinlang.org/
- **Compose:** https://developer.android.com/jetpack/compose
- **Retrofit:** https://square.github.io/retrofit/

---

**¡A seguir programando! 🚀**
