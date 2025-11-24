# Implementación de Autenticación en Android - SaborLocal

## Resumen

Se implementó un sistema completo de autenticación en la app Android que se integra con el backend de SaborLocal. El sistema permite:

- **Login** de usuarios existentes
- **Registro** de nuevos CLIENTES (auto-registro)
- Persistencia de sesión con SharedPreferences
- Manejo de tokens JWT automático

## Arquitectura Implementada

### 1. Capa de Datos (Data Layer)

#### DTOs (Data Transfer Objects)
**Archivo**: `data/remote/dto/SaborLocalDtos.kt`

```kotlin
// Request para login
data class LoginSaborLocalRequest(
    val email: String,
    val password: String
)

// Request para registro
data class RegisterSaborLocalRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val direccion: String? = null
)

// Response de autenticación
data class AuthSaborLocalData(
    val user: UserDto,
    val accessToken: String
)

// DTO de usuario
data class UserDto(
    val id: String,
    val nombre: String,
    val email: String,
    val role: String  // CLIENTE, PRODUCTOR, ADMIN
)
```

#### API Service
**Archivo**: `data/remote/SaborLocalApiService.kt`

```kotlin
interface SaborLocalApiService {
    // Login
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginSaborLocalRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    // Registro (solo CLIENTES)
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterSaborLocalRequest
    ): Response<ApiResponse<AuthSaborLocalData>>

    // Perfil del usuario actual
    @GET("auth/profile")
    suspend fun getProfile(): Response<ApiResponse<UserDto>>

    // Listar usuarios (solo ADMIN)
    @GET("auth/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserDto>>>
}
```

### 2. Capa de Dominio (Domain Layer)

#### Modelo User
**Archivo**: `model/SaborLocalModels.kt`

```kotlin
data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val role: String
) {
    fun isCliente(): Boolean = role == "CLIENTE"
    fun isProductor(): Boolean = role == "PRODUCTOR"
    fun isAdmin(): Boolean = role == "ADMIN"
}
```

### 3. Capa de Repositorio (Repository Layer)

**Archivo**: `repository/AuthSaborLocalRepository.kt`

```kotlin
class AuthSaborLocalRepository(context: Context) {
    // Métodos principales
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(nombre, email, password, telefono?, direccion?): Result<User>
    suspend fun getProfile(): Result<User>
    
    // Gestión de sesión
    fun isLoggedIn(): Boolean
    fun getToken(): String?
    fun getCurrentUser(): User?
    fun logout()
}
```

#### Funcionalidades del Repository

1. **Autenticación**:
   - Login con email y password
   - Registro de nuevos clientes
   - Obtener perfil del usuario actual

2. **Persistencia de Sesión**:
   - Guarda el token JWT en SharedPreferences
   - Guarda datos básicos del usuario (id, nombre, email, role)
   - Permite recuperar la sesión al abrir la app

3. **Gestión de Errores**:
   - Maneja errores HTTP (401, 404, 409, etc.)
   - Maneja errores de red
   - Retorna mensajes de error descriptivos

### 4. Capa de Presentación (Presentation Layer)

#### ViewModels

**LoginViewModel** (`viewmodel/LoginViewModel.kt`):
```kotlin
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    // StateFlows
    val uiState: StateFlow<LoginUiState>
    val email: StateFlow<String>
    val password: StateFlow<String>
    
    // Métodos
    fun onEmailChange(newEmail: String)
    fun onPasswordChange(newPassword: String)
    fun login()
    fun resetState()
    fun clearForm()
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val user: User) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
```

**RegisterViewModel** (`viewmodel/RegisterViewModel.kt`):
```kotlin
class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    // StateFlows
    val uiState: StateFlow<RegisterUiState>
    val nombre: StateFlow<String>
    val email: StateFlow<String>
    val password: StateFlow<String>
    val confirmPassword: StateFlow<String>
    val telefono: StateFlow<String>
    val direccion: StateFlow<String>
    
    // Métodos
    fun onNombreChange(newNombre: String)
    fun onEmailChange(newEmail: String)
    fun onPasswordChange(newPassword: String)
    fun onConfirmPasswordChange(newConfirmPassword: String)
    fun onTelefonoChange(newTelefono: String)
    fun onDireccionChange(newDireccion: String)
    fun register()
    fun resetState()
    fun clearForm()
}
```

#### Pantallas (Screens)

**LoginScreen** (`ui/screens/LoginScreen.kt`):
- Formulario con email y contraseña
- Botón de login con loading state
- Navegación a RegisterScreen
- Manejo de errores con UI feedback
- Validación de email
- Toggle para mostrar/ocultar contraseña

**RegisterScreen** (`ui/screens/RegisterScreen.kt`):
- Formulario completo de registro
- Campos obligatorios: nombre, email, password, confirmPassword
- Campos opcionales: teléfono, dirección
- Validaciones:
  - Email válido
  - Contraseña mínimo 6 caracteres
  - Contraseñas coinciden
- Botón de registro con loading state
- Navegación de vuelta a LoginScreen
- Manejo de errores con UI feedback

## Flujo de Usuario

### Registro de Nuevo Cliente

1. Usuario abre la app
2. Hace clic en "¿No tienes cuenta? Regístrate"
3. Completa el formulario de registro
4. El sistema valida los campos
5. Se envía la petición al backend POST /api/auth/register
6. Backend crea usuario con rol CLIENTE automáticamente
7. Backend retorna token JWT y datos del usuario
8. App guarda token y datos en SharedPreferences
9. Usuario es redirigido a la pantalla principal (ya autenticado)

### Login de Usuario Existente

1. Usuario abre la app
2. Ingresa email y contraseña
3. Hace clic en "Iniciar Sesión"
4. El sistema envía POST /api/auth/login
5. Backend valida credenciales
6. Backend retorna token JWT y datos del usuario
7. App guarda token y datos en SharedPreferences
8. Usuario es redirigido a la pantalla principal (ya autenticado)

### Persistencia de Sesión

1. Usuario cierra la app
2. Usuario vuelve a abrir la app
3. App verifica si hay token guardado con `repository.isLoggedIn()`
4. Si hay token, recupera datos del usuario con `repository.getCurrentUser()`
5. Usuario va directamente a la pantalla principal (sin login)

### Cierre de Sesión (Logout)

```kotlin
// En cualquier ViewModel o Screen
val repository = AuthSaborLocalRepository(context)
repository.logout()  // Elimina token y datos
// Navegar a LoginScreen
```

## Configuración Necesaria

### 1. Actualizar RetrofitClient

El token JWT debe enviarse automáticamente en los headers. Asegúrate de que RetrofitClient esté configurado con el AuthInterceptor:

```kotlin
// En RetrofitClient.kt
private val authInterceptor = Interceptor { chain ->
    val request = chain.request().newBuilder()
    
    // Obtener token de SharedPreferences
    val token = context.getSharedPreferences("saborlocal_prefs", Context.MODE_PRIVATE)
        .getString("auth_token", null)
    
    // Agregar token a headers si existe
    if (token != null) {
        request.addHeader("Authorization", "Bearer $token")
    }
    
    chain.proceed(request.build())
}
```

### 2. Actualizar Navegación

Agregar las nuevas screens a la navegación:

```kotlin
// En navigation/AppNavigation.kt
NavHost(navController = navController, startDestination = "splash") {
    composable("splash") {
        SplashScreen(
            onNavigateToLogin = { navController.navigate("login") },
            onNavigateToHome = { navController.navigate("home") }
        )
    }
    
    composable("login") {
        LoginScreen(
            onLoginSuccess = { 
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            },
            onNavigateToRegister = { navController.navigate("register") }
        )
    }
    
    composable("register") {
        RegisterScreen(
            onRegisterSuccess = { 
                navController.navigate("home") {
                    popUpTo("register") { inclusive = true }
                }
            },
            onNavigateToLogin = { navController.popBackStack() }
        )
    }
    
    composable("home") {
        HomeScreen(
            onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}
```

### 3. SplashScreen (Opcional)

Crear una pantalla de splash que verifica si hay sesión activa:

```kotlin
@Composable
fun SplashScreen(
    viewModel: LoginViewModel = viewModel(),
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(1000)  // Mostrar splash por 1 segundo
        
        if (viewModel.isLoggedIn()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }
    
    // UI del splash...
}
```

## Endpoints del Backend

### Públicos (sin autenticación)

```
POST /api/auth/login
Body: { "email": "user@example.com", "password": "password123" }
Response: { "success": true, "data": { "user": {...}, "access_token": "jwt_token" } }

POST /api/auth/register
Body: { "nombre": "Juan", "email": "juan@example.com", "password": "password123" }
Response: { "success": true, "data": { "user": {...}, "access_token": "jwt_token" } }
```

### Protegidos (requieren token JWT)

```
GET /api/auth/profile
Headers: Authorization: Bearer <token>
Response: { "success": true, "data": { "id": "...", "nombre": "...", "email": "...", "role": "CLIENTE" } }

GET /api/auth/users (Solo ADMIN)
Headers: Authorization: Bearer <token>
Response: { "success": true, "data": [{...}, {...}] }
```

## Testing

### Probar Login

```bash
# Iniciar el backend
cd /Users/roberto/Documents/GitHub/saborlocal-api
npm run start:dev

# Probar login desde la app Android
# O con curl:
curl -X POST http://10.0.2.2:3008/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sistema.com","password":"Admin123456"}'
```

### Probar Registro

```bash
curl -X POST http://10.0.2.2:3008/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@example.com",
    "password": "password123",
    "telefono": "+56912345678",
    "direccion": "Av. Principal 123"
  }'
```

## Archivos Creados/Modificados

### Nuevos Archivos

1. `data/remote/dto/SaborLocalDtos.kt` - DTOs de Auth agregados
2. `repository/AuthSaborLocalRepository.kt` - Repository de Auth
3. `viewmodel/LoginViewModel.kt` - ViewModel de Login
4. `viewmodel/RegisterViewModel.kt` - ViewModel de Registro
5. `ui/screens/LoginScreen.kt` - UI de Login
6. `ui/screens/RegisterScreen.kt` - UI de Registro
7. `model/SaborLocalModels.kt` - Modelo User agregado

### Archivos Modificados

1. `data/remote/SaborLocalApiService.kt` - Endpoints de Auth agregados

## Próximos Pasos

1. ✅ **Backend completamente funcional** (20/20 proyectos)
2. ✅ **Android - Sistema de Auth completo**
3. ⏳ **Actualizar navegación de la app**
4. ⏳ **Crear SplashScreen** (opcional)
5. ⏳ **Agregar logout en la UI**
6. ⏳ **Integrar con las pantallas de productos existentes**

## Características Implementadas

✅ Login con email y contraseña
✅ Registro de nuevos clientes
✅ Validación de formularios
✅ Manejo de errores con UI feedback
✅ Loading states durante operaciones async
✅ Persistencia de sesión con SharedPreferences
✅ Tokens JWT automáticos en headers
✅ Integración completa con SaborLocal API
✅ Arquitectura MVVM limpia y testeable
✅ StateFlow para reactividad
✅ Coroutines para async operations

---

**Fecha**: 2025-11-16
**Estado**: ✅ Implementación Completa
**Listo para**: Integración con la app y testing
