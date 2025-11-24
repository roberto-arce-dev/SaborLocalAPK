# 📘 Guía paso a paso – Migración a backend SaborLocal

Este documento resume, con acciones concretas, cómo reproducir la migración que reemplaza Room/local storage por el backend NestJS (_saborlocal-api_) en la app **MiAppModular**.

> **Requisitos previos**
>
> - Backend NestJS corriendo en `http://localhost:3008` (puerto por defecto de `saborlocal-api`).
> - Emulador Android (usa `10.0.2.2`) o dispositivo físico (usa IP de tu red).
> - Android Studio / Gradle funcionando.

---

## 1. Permitir HTTP local y configurar Retrofit

1. **Network Security Config**
   - Crea `app/src/main/res/xml/network_security_config.xml` con:
     ```xml
     <?xml version="1.0" encoding="utf-8"?>
     <network-security-config>
         <domain-config cleartextTrafficPermitted="true">
             <domain includeSubdomains="true">10.0.2.2</domain>
             <domain includeSubdomains="true">localhost</domain>
             <domain includeSubdomains="true">127.0.0.1</domain>
         </domain-config>
         <base-config cleartextTrafficPermitted="false">
             <trust-anchors>
                 <certificates src="system" />
             </trust-anchors>
         </base-config>
     </network-security-config>
     ```
   - En `app/src/main/AndroidManifest.xml`, agrega el atributo al `application`:
     ```xml
     <application
         ...
         android:theme="@style/Theme.MiAppModular"
         android:networkSecurityConfig="@xml/network_security_config">
     ```

2. **Retrofit y AuthInterceptor**
   - En `RetrofitClient.kt`:
     ```kotlin
     private const val BASE_URL = "http://10.0.2.2:3008/api/"

     private lateinit var sessionManager: SessionManager
     private lateinit var context: Context

     fun initialize(context: Context) {
         this.context = context.applicationContext
         sessionManager = SessionManager(context.applicationContext)
     }

     private val okHttpClient: OkHttpClient by lazy {
         val authInterceptor = AuthInterceptor(sessionManager, context)
         ...
     }

     val saborLocalApiService: SaborLocalApiService by lazy {
         retrofit.create(SaborLocalApiService::class.java)
     }
     ```
   - `AuthInterceptor.kt` debe leer el token primero de `SharedPreferences`:
     ```kotlin
     private val saborLocalPrefs by lazy {
         context.getSharedPreferences("saborlocal_prefs", Context.MODE_PRIVATE)
     }

     override fun intercept(chain: Interceptor.Chain): Response {
         var token = saborLocalPrefs.getString("auth_token", null)
         if (token.isNullOrEmpty()) {
             token = runBlocking { sessionManager.getAuthToken() }
         }
         ...
     }
     ```

---

## 2. Capa de datos para SaborLocal

1. **DTOs y requests** – crea `app/src/main/java/.../data/remote/dto/SaborLocalDtos.kt` con `ApiResponse`, `ProductoDto`, `RegisterSaborLocalRequest`, etc.

2. **SaborLocalApiService** – en `data/remote/SaborLocalApiService.kt` define endpoints:
   ```kotlin
   @POST("auth/login")
   suspend fun login(@Body request: LoginSaborLocalRequest): Response<ApiResponse<AuthSaborLocalData>>

   @GET("producto")
   suspend fun getProductos(): Response<ApiResponse<List<ProductoDto>>>
   // ... product, productor, cliente, pedido, entrega, upload
   ```

3. **Modelos de dominio** – `model/SaborLocalModels.kt` con `User`, `Producto`, `Productor`, etc. Exponiendo helpers como `Productor.getImagenUrl()` y enums `EstadoPedido`.

4. **Mappers** – `data/mapper/SaborLocalMappers.kt` con extensiones `ProductoDto.toDomain()`, `List<ProductorDto>.toProductorDomainList()`…

---

## 3. Repositorios

1. **AuthSaborLocalRepository** (`repository/AuthSaborLocalRepository.kt`)
   - Usa `RetrofitClient.saborLocalApiService`.
   - Persiste: token, id, nombre, email, role mediante `SharedPreferences`.
   - Métodos clave:
     ```kotlin
     suspend fun login(email: String, password: String): Result<User>
     suspend fun register(nombre: String, email: String, password: String, telefono: String?, direccion: String?): Result<User>
     suspend fun createProductorUser(...): Result<User>   // requiere ADMIN
     suspend fun getAllUsers(): Result<List<User>>
     fun isLoggedIn(): Boolean
     fun getCurrentUser(): User?
     fun logout()
     ```

2. **Repos de catálogo**
   - `ProductoRepository.kt`: CRUD, subida de imagen usando `MultipartBody`.
   - `ProductorRepository.kt`: CRUD de productores (a través de endpoints públicos/protegidos).

3. **Eliminar dependencias Room**
   - Comenta el antiguo `UserRepository` y referencias en `AppDependencies`.
   - Mantén `SessionManager` y `AvatarRepository` si aún se usan.

---

## 4. ViewModels de autenticación

1. **LoginViewModel.kt**
   ```kotlin
   class LoginViewModel(application: Application) : AndroidViewModel(application) {
       private val repository = AuthSaborLocalRepository(application)

       private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
       val uiState: StateFlow<LoginUiState> = _uiState

       private val _email = MutableStateFlow("")
       private val _password = MutableStateFlow("")

       fun login() {
           if (_email.value.isBlank() || _password.value.isBlank()) {
               _uiState.value = LoginUiState.Error("Por favor completa todos los campos")
               return
           }
           if (!Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
               _uiState.value = LoginUiState.Error("Email inválido")
               return
           }
           _uiState.value = LoginUiState.Loading
           viewModelScope.launch {
               val result = repository.login(_email.value, _password.value)
               _uiState.value = result.fold(
                   onSuccess = { LoginUiState.Success(it) },
                   onFailure = { LoginUiState.Error(it.message ?: "Error desconocido") }
               )
           }
       }
   }
   ```

2. **RegisterViewModel.kt**
   - Misma estructura, pero con flows para `nombre`, `telefono`, `direccion`.
   - Validaciones:
     - `nombre/email/password` obligatorios.
     - Password ≥ 6, coincide con confirmación.
     - `ValidationUtils.validatePhone()` para productores (opcional en clientes).
   - Usa `repository.register()` y expone `RegisterUiState`.

3. **ValidationUtils**
   - Añade helper `validatePhone(String)` con reglas de longitud 8–15 dígitos.

---

## 5. Pantallas Compose de Login y Registro

1. **LoginScreen.kt**
   - Consume los `StateFlow` del ViewModel con `collectAsState()`.
   - Controles:
     ```kotlin
     OutlinedTextField(
         value = email,
         onValueChange = viewModel::onEmailChange,
         label = { Text("Email") },
         leadingIcon = { Icon(Icons.Default.Email, null) },
         enabled = uiState !is LoginUiState.Loading
     )
     ```
   - Botones `Button` (login) + `OutlinedButton` (navegar a registro).
   - `LaunchedEffect(uiState)` para detectar `LoginUiState.Success` y disparar `onLoginSuccess()`.

2. **RegisterScreen.kt**
   - Incluye campos extras (`Teléfono`, `Dirección`).
   - Botón back en la `TopAppBar` que llama `onNavigateToLogin`.
   - Usa `RegisterUiState` para `CircularProgressIndicator` y mensajes de error.

---

## 6. Flujo de arranque (Splash) y navegación

1. **SplashViewModel.kt**
   ```kotlin
   private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Checking)
   init {
       viewModelScope.launch {
           delay(1000)   // opcional
           val isLoggedIn = repository.isLoggedIn()
           _navigationState.value = if (isLoggedIn && repository.getCurrentUser() != null) {
               SplashNavigationState.NavigateToHome
           } else {
               SplashNavigationState.NavigateToLogin
           }
       }
   }
   ```

2. **SplashScreen.kt**
   - Observa `navigationState` y llama a `onNavigateToHome()` u `onNavigateToLogin()`.
   - Muestra icono/logo + spinner.

3. **AppNavigation.kt**
   - Cambia `startDestination = "splash"`.
   - Registra rutas: `"login"`, `"register"`, `"home"`, `"profile"`, `"productos_list"`, `"create_producto"`, `"create_productor"`, `"productores_list"`.
   - Usa `navController.navigate("home") { popUpTo("login") { inclusive = true } }` tras login/registro.

---

## 7. Home y nuevos módulos de catálogo

1. **HomeScreen.kt**
   - Reemplaza tarjetas antiguas por accesos directos:
     ```kotlin
     FeatureModuleCard(
         icon = Icons.Filled.ShoppingCart,
         title = "Productos",
         onClick = onNavigateToProductosList
     )
     ```
   - Añade callbacks `onNavigateToProductosList`, `onNavigateToCreateProducto`, `onNavigateToCreateProductor`, `onNavigateToProductoresList`.

2. **Pantallas nuevas**
   - `CreateProductoScreen` / `CreateProductoViewModel`: formulario con `nombre`, `descripcion`, `precio`, `unidad`, `stock`, `productorId`. (Por ahora el `productorId` se ingresa manualmente; futura mejora: obtenerlo del usuario logueado).
   - `CreateProductorScreen` / `CreateProductorViewModel`: formulario exclusivo de ADMIN que usa `AuthSaborLocalRepository.createProductorUser`.
   - `ProductosListScreen` / `ProductosListViewModel`: muestra lista filtrable por nombre/productor/rango de precios.
   - `ProductoresListScreen` / `ProductoresListViewModel`: lista de usuarios con rol PRODUCTOR, incluye búsqueda y botón (pendiente) para crear productor.
   - `ProductoViewModel`: ViewModel genérico para CRUD y subida de imágenes; útil para pantallas de detalle si las necesitas.

---

## 8. Documentación y pruebas

1. **Documentos agregados**
   - `ANDROID_AUTH_IMPLEMENTATION.md`: detalle completo de la capa de autenticación.
   - `TUTORIAL_MIGRACION_BACKEND_SABORLOCAL.md`: explicación conceptual de la migración.
   - `TUTORIAL_ROOM_COMPLETO.md`: referencia histórica de Room.
   - `TESTING_GUIDE.md`: pasos para crear tests unitarios de repositorios y viewmodels (MockK, Turbine, coroutines test).

2. **Guía rápida de testing (desde `TESTING_GUIDE.md`)**
   - Añade dependencias de `junit`, `kotlinx-coroutines-test`, `mockk`, `turbine`.
   - Estructura carpetas `app/src/test/java/com/example/miappmodular/{repository,viewmodel}`.
   - Tests sugeridos:
     - `AuthSaborLocalRepositoryTest`: login/register exitoso y con errores.
     - `LoginViewModelTest`, `RegisterViewModelTest`: validaciones, estados `Loading` y `Success/Error`.

---

## 9. Checklist final

- [ ] Ejecutar `RetrofitClient.initialize(applicationContext)` en tu clase `Application`.
- [ ] Confirmar que el backend responde (`curl http://localhost:3008/api/health`).
- [ ] Lanzar la app: `./gradlew installDebug` o desde Android Studio.
- [ ] Probar `Login`, `Registro`, navegación desde `Splash`.
- [ ] Probar CRUD de productos/productores (según roles disponibles).
- [ ] Correr tests: `./gradlew test`.

> Una vez seguidos estos pasos, la app quedará preparada para autenticarse contra SaborLocal, consumir datos del backend y ofrecer los módulos de catálogo descritos.

¡Listo! Repite cada paso cuando quieras rehacer la migración desde un árbol limpio. Guarda este documento junto al proyecto para futuras referencias.
