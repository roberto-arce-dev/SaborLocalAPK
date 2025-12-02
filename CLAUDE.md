# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**MiAppModular** (SaborLocal) is a native Android e-commerce app built with **Kotlin** and **Jetpack Compose** for connecting local food producers with customers. The app follows **Clean Architecture** principles with clear separation between data, domain, and presentation layers.

**Tech Stack:**
- **UI:** Jetpack Compose with Material 3
- **Navigation:** Compose Navigation
- **Networking:** Retrofit + OkHttp
- **Data:** Room (configured but not currently used), EncryptedSharedPreferences for secure token storage
- **DI:** Manual dependency injection (no Hilt/Dagger)
- **Testing:** JUnit, MockK, Kotlin Coroutines Test

## Build Commands

### Building the App
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device/emulator
./gradlew installDebug
```

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run unit tests for a specific class
./gradlew test --tests AuthSaborLocalRepositoryTest

# Run unit tests with detailed output
./gradlew test --info

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Development Workflow
```bash
# Run app on emulator/device (via Android Studio)
# Click the green "Run" button or use Shift+F10

# View Logcat in Android Studio
# View > Tool Windows > Logcat

# Refresh Gradle dependencies
./gradlew clean --refresh-dependencies
```

## Architecture Overview

The app follows a **domain-centric architecture** organized by feature domains (auth, producto, pedido, etc.):

```
app/src/main/java/com/example/miappmodular/
├── MainActivity.kt              # Single Activity entry point
├── data/                        # Data layer
│   ├── local/                   # Local data management
│   │   ├── TokenManager.kt     # JWT token + user data (EncryptedSharedPreferences)
│   │   ├── SessionManager.kt   # Session state management
│   │   └── CarritoManager.kt   # Shopping cart state (in-memory)
│   ├── remote/                  # Remote API layer
│   │   ├── RetrofitClient.kt   # Singleton Retrofit configuration
│   │   ├── AuthInterceptor.kt  # Auto-injects JWT tokens
│   │   ├── api/                # API service interfaces (by domain)
│   │   │   ├── SaborLocalAuthApiService.kt
│   │   │   ├── SaborLocalProductoApiService.kt
│   │   │   ├── SaborLocalProductorApiService.kt
│   │   │   ├── SaborLocalPedidoApiService.kt
│   │   │   └── ...
│   │   └── dto/                # Data Transfer Objects (by domain)
│   │       ├── auth/           # Auth DTOs (LoginRequest, RegisterRequest, UserDto, etc.)
│   │       ├── producto/       # Product DTOs (ProductoDto, CreateProductoRequest, etc.)
│   │       ├── pedido/         # Order DTOs (PedidoDto, CreatePedidoRequest, etc.)
│   │       └── ...
│   └── mapper/                  # DTO ↔ Domain model converters
│       ├── ProductoMapper.kt
│       ├── PedidoMapper.kt
│       └── SaborLocalMappers.kt
├── model/                       # Domain models (business logic)
│   └── SaborLocalModels.kt     # User, Producto, Pedido, Productor, etc.
├── repository/                  # Repository pattern (data orchestration)
│   ├── AuthSaborLocalRepository.kt
│   ├── ProductoRepository.kt
│   ├── ProductorRepository.kt
│   ├── PedidoRepository.kt
│   └── ...
├── viewmodel/                   # ViewModels (UI state management)
│   ├── LoginViewModel.kt
│   ├── RegisterViewModel.kt
│   ├── ProductosListViewModel.kt
│   └── ...
└── ui/                          # Presentation layer
    ├── navigation/              # Navigation graph
    │   ├── AppNavigation.kt    # Main nav graph (splash → login → home)
    │   └── MainScreen.kt       # Bottom nav container (5 tabs)
    ├── screens/                 # Composable screens
    │   ├── SplashScreen.kt
    │   ├── LoginScreen.kt
    │   ├── RegisterScreen.kt
    │   ├── HomeScreen.kt
    │   ├── ProductosListScreen.kt
    │   ├── CarritoScreen.kt
    │   └── ...
    ├── components/              # Reusable UI components
    │   ├── FloatingNavBar.kt
    │   ├── ProductDetailSheet.kt
    │   └── ...
    └── theme/                   # Material 3 theming
        ├── Theme.kt            # Light/Dark color schemes
        ├── Color.kt            # Color definitions
        ├── Type.kt             # Typography
        └── ComponentVariants.kt
```

## Key Architectural Patterns

### 1. Domain-Organized DTOs and API Services

DTOs and API services are **organized by business domain** (not by layer), making it easier to understand feature boundaries:

```
data/remote/
├── dto/
│   ├── auth/                   # All auth-related DTOs
│   │   ├── LoginRequest.kt
│   │   ├── RegisterSaborLocalRequest.kt
│   │   └── UserDto.kt
│   ├── producto/               # All product-related DTOs
│   │   ├── ProductoDto.kt
│   │   ├── CreateProductoRequest.kt
│   │   └── UpdateProductoRequest.kt
│   └── pedido/                 # All order-related DTOs
│       ├── PedidoDto.kt
│       ├── CreatePedidoRequest.kt
│       └── PedidoItemDto.kt
└── api/
    ├── SaborLocalAuthApiService.kt      # Auth endpoints
    ├── SaborLocalProductoApiService.kt  # Product endpoints
    └── SaborLocalPedidoApiService.kt    # Order endpoints
```

**When adding new features:**
- Create a new subdirectory in `dto/` for the domain (e.g., `dto/notificacion/`)
- Create a corresponding API service (e.g., `SaborLocalNotificacionApiService.kt`)
- Register the service in `RetrofitClient.kt` as a lazy property

### 2. Data Flow Architecture

```
UI (Composable) → ViewModel → Repository → API Service → Retrofit → Backend
                                ↓                ↓
                            Mappers          DTO
                                ↓
                          Domain Model
```

**Key principles:**
- **DTOs** (`data/remote/dto/`) represent the exact API response structure
- **Domain Models** (`model/`) represent business entities (nullable fields handled properly)
- **Mappers** (`data/mapper/`) convert DTOs to domain models using extension functions
- **Repositories** orchestrate data operations and return `Result<T>` sealed classes
- **ViewModels** manage UI state and handle user interactions

### 3. Token Management

The app uses **EncryptedSharedPreferences** for secure token storage (not DataStore):

**Why EncryptedSharedPreferences?**
- `AuthInterceptor` runs on OkHttp's network threads (cannot use suspend functions)
- EncryptedSharedPreferences is synchronous AND secure (hardware-backed AES256_GCM)
- Simpler pattern for students: `TokenManager(context)` with no DI complexity

**TokenManager responsibilities:**
- Store/retrieve JWT tokens securely
- Store/retrieve user data (id, name, email, role)
- Check authentication state (`isLoggedIn()`)
- Provide current user data (`getCurrentUser()`)

**Critical initialization pattern:**
```kotlin
// MainActivity.kt
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // MUST initialize before setContent {}
    RetrofitClient.initialize(this)

    setContent { /* ... */ }
}
```

### 4. Navigation Architecture

**Two-level navigation system:**

1. **App-level navigation** (`AppNavigation.kt`):
   - `splash` → Checks token → `login` or `home`
   - `login` → Successful login → `home` (backstack cleared)
   - `register` → Successful registration → `home` (backstack cleared)
   - Logout → `login` (backstack fully cleared)

2. **Bottom navigation** (`MainScreen.kt`):
   - Home, Productos, Carrito, Pedidos, Perfil (5 tabs)
   - Only accessible after authentication

**Backstack management:**
```kotlin
// Clear backstack on authentication success
navController.navigate("home") {
    popUpTo("login") { inclusive = true }
}

// Full backstack clear on logout
navController.navigate("login") {
    popUpTo(0) { inclusive = true }
    launchSingleTop = true
}
```

### 5. API Configuration

**Base URL:** `https://saborloca-api.onrender.com/api/`

**Important notes:**
- **Timeouts increased for Render.com free tier:** 60s connect, 90s read, 60s write (cold starts)
- For local development with emulator: Use `http://10.0.2.2:PORT/api/` (special emulator IP)
- For physical devices: Use `http://192.168.1.X:PORT/api/` (your computer's LAN IP)

**Network security:** Configured in `app/src/main/res/xml/network_security_config.xml` to allow cleartext traffic to localhost for development.

### 6. State Management Pattern

ViewModels use **StateFlow** for reactive UI updates:

```kotlin
// ViewModel pattern
private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// Screen pattern
val uiState by viewModel.uiState.collectAsState()
when (val state = uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> Content(state.data)
    is UiState.Error -> ErrorMessage(state.message)
}
```

### 7. Material 3 Theming

**Color scheme inspired by fresh, local produce:**
- **Primary (Verde Bosque):** Forest green `#2D6A4F` - freshness and sustainability
- **Secondary (Naranja Terracota):** Terracotta orange `#E76F51` - warmth and community
- **Tertiary (Verde Menta):** Mint green `#52B788` - organic and healthy
- **Background (Crema):** Natural cream `#FFF8F0` - authenticity and simplicity

**Shapes:** Rounded corners `(6dp/8dp/12dp/16dp/24dp)` to evoke organic produce forms.

**Dark mode:** Fully supported with warm tones maintaining brand identity.

## Testing Strategy

**Unit tests location:** `app/src/test/java/com/example/miappmodular/`

**Current test coverage:**
- `repository/AuthSaborLocalRepositoryTest.kt` - Auth repository with MockK

**Testing pattern:**
```kotlin
@Test
fun `test name describing what it tests`() = runTest {
    // Given: Setup test data and mocks
    coEvery { apiService.login(any()) } returns Response.success(mockDto)

    // When: Execute the function
    val result = repository.login(email, password)

    // Then: Verify results
    assertTrue(result is Result.Success)
    coVerify { tokenManager.saveToken(any(), any()) }
}
```

**Common testing utilities:**
- **MockK** for mocking dependencies
- **Kotlin Coroutines Test** for testing suspend functions
- `runTest {}` for coroutine-based tests

## Common Development Patterns

### Adding a New Feature

1. **Define domain model** in `model/SaborLocalModels.kt`
2. **Create DTOs** in `data/remote/dto/[domain]/`
3. **Create API service** in `data/remote/api/Sabor Local[Domain]ApiService.kt`
4. **Register service** in `RetrofitClient.kt` as a lazy property
5. **Create mappers** in `data/mapper/[Domain]Mapper.kt`
6. **Create repository** in `repository/[Domain]Repository.kt`
7. **Create ViewModel** in `viewmodel/[Feature]ViewModel.kt`
8. **Create screen** in `ui/screens/[Feature]Screen.kt`
9. **Add navigation** in `AppNavigation.kt` or `MainScreen.kt`
10. **Write tests** in `app/src/test/`

### Working with Nullable Fields

**Domain models use nullable fields appropriately:**
```kotlin
data class User(
    val id: String,
    val nombre: String? = null,  // May be null in API response
    val email: String,
    val role: String
) {
    // Helper method for safe display
    fun getDisplayName(): String = nombre ?: email.substringBefore("@")
}
```

**Always handle nullability in mappers:**
```kotlin
fun UserDto.toModel(): User {
    return User(
        id = id,
        nombre = nombre,  // Keep nullability
        email = email,
        role = role
    )
}
```

### Error Handling Pattern

Use `Result<T>` sealed class for consistent error handling:

```kotlin
// Repository
suspend fun getProductos(): Result<List<Producto>> {
    return try {
        val response = apiService.getProductos()
        if (response.isSuccessful && response.body() != null) {
            val productos = response.body()!!.map { it.toModel() }
            Result.Success(productos)
        } else {
            Result.Error("Error: ${response.code()} ${response.message()}")
        }
    } catch (e: Exception) {
        Result.Error("Error de conexión: ${e.message}", e)
    }
}

// ViewModel
viewModelScope.launch {
    when (val result = repository.getProductos()) {
        is Result.Success -> _uiState.value = UiState.Success(result.data)
        is Result.Error -> _uiState.value = UiState.Error(result.message)
    }
}
```

## Important Conventions

1. **No Dependency Injection framework** - Use manual DI with singleton objects and constructor injection
2. **Lazy initialization** - Use `by lazy {}` for expensive objects in RetrofitClient
3. **Suspend functions** - All repository methods that make network calls are `suspend`
4. **StateFlow over LiveData** - Use Kotlin Flow for reactive state management
5. **Sealed classes for state** - Use sealed classes/interfaces for UiState and Result types
6. **Extension functions for mapping** - `fun DtoType.toModel(): ModelType`
7. **Single Activity architecture** - MainActivity hosts all Compose navigation
8. **Material 3 theming** - Always use `MaterialTheme.colorScheme` colors, never hardcoded colors

## Permissions and Security

**Declared permissions:**
- `INTERNET` - Network requests
- `ACCESS_NETWORK_STATE` - Check connectivity
- `CAMERA` - Profile photo capture
- `READ_MEDIA_IMAGES` - Gallery access (Android 13+)
- `READ_EXTERNAL_STORAGE` - Gallery access (Android 12 and below)

**FileProvider configured** for camera image capture at `${applicationId}.fileprovider`.

**Network security:** Cleartext traffic allowed for localhost development (configured in `network_security_config.xml`).

## Code Style Notes

- **Comments in Spanish** - Domain concepts and user-facing text in Spanish
- **Code in English** - Variable names, function names, technical terms in English
- **Extensive KDoc** - Most classes have detailed KDoc comments explaining architecture
- **Examples in comments** - Many files include usage examples in their KDoc
