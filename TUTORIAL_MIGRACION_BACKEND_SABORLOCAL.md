# 🌱 Tutorial: Migración de Room a Backend SaborLocal API

**Fecha:** 2025-11-16
**Objetivo:** Conectar app Android con backend NestJS (saborlocal-api)
**Nivel:** Intermedio

---

## 📚 Tabla de Contenidos

1. [Introducción](#introducción)
2. [Arquitectura del Proyecto](#arquitectura-del-proyecto)
3. [Paso 1: Eliminar Room Database](#paso-1-eliminar-room-database)
4. [Paso 2: Configurar DTOs](#paso-2-configurar-dtos)
5. [Paso 3: Crear ApiService](#paso-3-crear-apiservice)
6. [Paso 4: Configurar Retrofit](#paso-4-configurar-retrofit)
7. [Paso 5: Crear Modelos de Dominio](#paso-5-crear-modelos-de-dominio)
8. [Paso 6: Crear Mappers](#paso-6-crear-mappers)
9. [Paso 7: Crear Repositorios](#paso-7-crear-repositorios)
10. [Paso 8: Crear ViewModels](#paso-8-crear-viewmodels)
11. [Paso 9: Crear Screens](#paso-9-crear-screens)
12. [Paso 10: Configurar Navegación](#paso-10-configurar-navegación)
13. [Testing y Depuración](#testing-y-depuración)

---

## Introducción

### ¿Qué vamos a hacer?

Migraremos una app Android que usa **Room Database** (almacenamiento local) para que consuma datos de un **backend NestJS** real (saborlocal-api) que corre en `http://localhost:3008`.

### ¿Por qué migrar?

| **Room (Local)** | **Backend API (Remoto)** |
|------------------|--------------------------|
| ❌ Datos solo en el dispositivo | ✅ Datos compartidos entre usuarios |
| ❌ No hay sincronización | ✅ Sincronización automática |
| ❌ Difícil escalar | ✅ Escalable y profesional |
| ✅ Offline-first | ⚠️ Requiere conexión (o cache) |

### ¿Qué aprenderás?

- ✅ Conectar Android con API REST usando Retrofit
- ✅ Implementar arquitectura MVVM con backend
- ✅ Manejar estados de carga, éxito y error
- ✅ Subir imágenes desde Android al backend
- ✅ Usar Flows para reactive UI
- ✅ Separar DTOs, Models y Mappers

---

## Arquitectura del Proyecto

### Estructura de Capas

```
┌─────────────────────────────────────┐
│         UI (Jetpack Compose)        │
│  ProductosListScreen, ProductoDetail│
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          ViewModels                 │
│   ProductoViewModel, ProductorVM    │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│         Repositories                │
│  ProductoRepository, ProductorRepo  │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│      ApiService (Retrofit)          │
│  SaborLocalApiService + RetrofitClient│
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│     Backend (NestJS + MongoDB)      │
│  http://10.0.2.2:3008/api/          │
└─────────────────────────────────────┘
```

### Flujo de Datos

```
[UI Click]
  → ViewModel.loadProductos()
    → Repository.getProductos()
      → ApiService.getProductos() [HTTP GET]
        → Backend responde JSON
      ← ApiResponse<List<ProductoDto>>
    ← Mapper: ProductoDto → Producto (Domain Model)
  ← StateFlow emite nueva lista
[UI se actualiza automáticamente]
```

---

## Paso 1: Eliminar Room Database

### ¿Por qué eliminar Room?

Ya no necesitamos almacenamiento local porque vamos a obtener los datos directamente del backend.

### Archivos a eliminar

```bash
# Desde la raíz del proyecto Android:
cd app/src/main/java/com/example/miappmodular

# Eliminar carpetas de Room
rm -rf data/local/dao
rm -rf data/local/database
rm -rf data/local/entity
```

### Actualizar build.gradle.kts

**Antes:**
```kotlin
// Room - Para Módulo 3
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)
```

**Después (comentar o eliminar):**
```kotlin
// Room eliminado - ahora usamos backend
// implementation(libs.androidx.room.runtime)
// implementation(libs.androidx.room.ktx)
// ksp(libs.androidx.room.compiler)
```

### ✅ Verificación

- [ ] Carpetas `dao`, `database`, `entity` eliminadas
- [ ] Dependencias de Room comentadas
- [ ] Sync Gradle sin errores

---

## Paso 2: Configurar DTOs

### ¿Qué son los DTOs?

**DTO (Data Transfer Object)**: Clase que mapea EXACTAMENTE la estructura JSON que el backend retorna.

### Crear DTOs

**Ubicación:** `data/remote/dto/SaborLocalDtos.kt`

```kotlin
package com.example.miappmodular.data.remote.dto

import com.google.gson.annotations.SerializedName

// Wrapper genérico para respuestas del API
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val total: Int? = null
)

// DTO para Productor
data class ProductorDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)

// DTO para Producto
data class ProductoDto(
    @SerializedName("_id")
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: Any,  // Puede ser String (ID) o ProductorDto (populated)
    val imagen: String? = null,
    val imagenThumbnail: String? = null
) {
    // Helper para obtener el productor si fue populated
    fun getProductorPopulated(): ProductorDto? {
        return when (productor) {
            is Map<*, *> -> {
                val map = productor as Map<String, Any>
                ProductorDto(
                    id = map["_id"] as? String ?: "",
                    nombre = map["nombre"] as? String ?: "",
                    ubicacion = map["ubicacion"] as? String ?: "",
                    telefono = map["telefono"] as? String ?: "",
                    email = map["email"] as? String ?: "",
                    imagen = map["imagen"] as? String,
                    imagenThumbnail = map["imagenThumbnail"] as? String
                )
            }
            else -> null
        }
    }
}

// Request DTOs
data class CreateProductoRequest(
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: String  // ID del productor
)

data class UpdateProductoRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    val stock: Int? = null
)
```

### ⚠️ Nota Importante: @SerializedName

```kotlin
@SerializedName("_id")  // MongoDB usa "_id"
val id: String           // Kotlin usa "id"
```

Gson usa esta anotación para mapear `_id` (JSON del backend) a `id` (propiedad Kotlin).

### ✅ Verificación

- [ ] DTOs creados en `data/remote/dto/SaborLocalDtos.kt`
- [ ] @SerializedName usado para `_id`
- [ ] Helpers para populate agregados

---

## Paso 3: Crear ApiService

### ¿Qué es ApiService?

Interfaz que define todos los endpoints del API usando anotaciones de Retrofit.

### Crear SaborLocalApiService

**Ubicación:** `data/remote/SaborLocalApiService.kt`

```kotlin
package com.example.miappmodular.data.remote

import com.example.miappmodular.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SaborLocalApiService {

    // ==================== PRODUCTO ====================

    @GET("producto")
    suspend fun getProductos(): Response<ApiResponse<List<ProductoDto>>>

    @GET("producto/{id}")
    suspend fun getProducto(@Path("id") id: String): Response<ApiResponse<ProductoDto>>

    @POST("producto")
    suspend fun createProducto(@Body request: CreateProductoRequest): Response<ApiResponse<ProductoDto>>

    @PATCH("producto/{id}")
    suspend fun updateProducto(
        @Path("id") id: String,
        @Body request: UpdateProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    @DELETE("producto/{id}")
    suspend fun deleteProducto(@Path("id") id: String): Response<ApiResponse<Unit>>

    @Multipart
    @POST("producto/{id}/upload-image")
    suspend fun uploadProductoImage(
        @Path("id") id: String,
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<ProductoDto>>

    // ==================== PRODUCTOR ====================

    @GET("productor")
    suspend fun getProductores(): Response<ApiResponse<List<ProductorDto>>>

    @GET("productor/{id}")
    suspend fun getProductor(@Path("id") id: String): Response<ApiResponse<ProductorDto>>

    // ... más endpoints
}
```

### Anotaciones de Retrofit

| Anotación | Descripción | Ejemplo |
|-----------|-------------|---------|
| `@GET` | HTTP GET | `@GET("producto")` → GET /api/producto |
| `@POST` | HTTP POST | `@POST("producto")` → POST /api/producto |
| `@PATCH` | HTTP PATCH | `@PATCH("producto/{id}")` |
| `@DELETE` | HTTP DELETE | `@DELETE("producto/{id}")` |
| `@Path` | Parámetro en URL | `@Path("id") id: String` |
| `@Body` | Cuerpo JSON | `@Body request: CreateProductoRequest` |
| `@Multipart` | Subida de archivos | Para imágenes |
| `@Part` | Parte de multipart | `@Part file: MultipartBody.Part` |

### ✅ Verificación

- [ ] ApiService creado con todos los endpoints
- [ ] Métodos marcados como `suspend` para coroutines
- [ ] Retornan `Response<ApiResponse<T>>` para manejo manual de errores

---

## Paso 4: Configurar Retrofit

### ¿Qué es Retrofit?

Biblioteca que convierte tu ApiService (interfaz) en un cliente HTTP funcional.

### Actualizar RetrofitClient

**Ubicación:** `data/remote/RetrofitClient.kt`

**Cambiar BASE_URL:**

```kotlin
object RetrofitClient {

    // IMPORTANTE: 10.0.2.2 es la IP que el emulador usa para acceder a localhost
    // Si usas dispositivo físico, usa la IP de tu PC (ej: 192.168.1.X)
    private const val BASE_URL = "http://10.0.2.2:3008/api/"

    private lateinit var sessionManager: SessionManager

    fun initialize(context: Context) {
        sessionManager = SessionManager(context.applicationContext)
    }

    private val okHttpClient: OkHttpClient by lazy {
        val authInterceptor = AuthInterceptor(sessionManager)
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

### Importante: IPs para Testing

| Contexto | IP a usar | Ejemplo |
|----------|-----------|---------|
| Emulador Android | `10.0.2.2` | `http://10.0.2.2:3008/api/` |
| Dispositivo Físico | IP de tu PC en WiFi | `http://192.168.1.5:3008/api/` |
| Producción | Dominio real | `https://api.saborlocal.com/api/` |

Para obtener tu IP en WiFi:
```bash
# macOS/Linux
ifconfig | grep "inet "

# Windows
ipconfig
```

### ✅ Verificación

- [ ] BASE_URL apunta a `http://10.0.2.2:3008/api/`
- [ ] saborLocalApiService creado como lazy property
- [ ] Logging habilitado para debug

---

## Paso 5: Crear Modelos de Dominio

### ¿Por qué necesitamos Models además de DTOs?

| Aspecto | DTO | Model (Domain) |
|---------|-----|----------------|
| **Propósito** | Mapear JSON del API | Representar lógica de negocio |
| **Acoplamiento** | Al API (si API cambia, DTO cambia) | Independiente del API |
| **Métodos** | Solo getters/setters | Lógica de negocio (ej: `getPrecioFormateado()`) |
| **Uso** | Solo en capa de red | En toda la app (UI, ViewModels) |

### Crear Models

**Ubicación:** `model/SaborLocalModels.kt`

```kotlin
package com.example.miappmodular.model

data class Productor(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
) {
    fun getImagenUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagen?.let { "$baseUrl/$it" }
    }

    fun getThumbnailUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagenThumbnail?.let { "$baseUrl/$it" }
    }
}

data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: Productor,  // Siempre completo, no nullable
    val imagen: String? = null,
    val imagenThumbnail: String? = null
) {
    fun getPrecioFormateado(): String {
        return "$${"%.2f".format(precio)} / $unidad"
    }

    fun tieneStock(): Boolean = stock > 0

    fun getImagenUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagen?.let { "$baseUrl/$it" }
    }

    fun stockBajo(): Boolean = stock in 1..9
}

// Resultado de operaciones
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
}
```

### Ventajas de los Métodos Helper

```kotlin
// Sin helper
Text("${producto.precio} / ${producto.unidad}")  // "15.5 / kg"

// Con helper
Text(producto.getPrecioFormateado())  // "$15.50 / kg"
```

### ✅ Verificación

- [ ] Models creados en `model/SaborLocalModels.kt`
- [ ] Métodos helpers agregados (getPrecioFormateado, getImagenUrl, etc.)
- [ ] Sealed class Result<T> para manejar Success/Error

---

## Paso 6: Crear Mappers

### ¿Qué son los Mappers?

Funciones de extensión que convierten DTOs → Models.

### Crear Mappers

**Ubicación:** `data/mapper/SaborLocalMappers.kt`

```kotlin
package com.example.miappmodular.data.mapper

import com.example.miappmodular.data.remote.dto.*
import com.example.miappmodular.model.*

// DTO → Model
fun ProductorDto.toDomain(): Productor {
    return Productor(
        id = id,
        nombre = nombre,
        ubicacion = ubicacion,
        telefono = telefono,
        email = email,
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

fun ProductoDto.toDomain(): Producto? {
    val productorPopulated = getProductorPopulated() ?: return null

    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        unidad = unidad,
        stock = stock,
        productor = productorPopulated.toDomain(),
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

// Extension para listas
fun List<ProductoDto>.toDomainList(): List<Producto> {
    return mapNotNull { it.toDomain() }
}
```

### Uso de Mappers

```kotlin
// En Repository:
val response = apiService.getProductos()
val productosDto = response.body()?.data  // List<ProductoDto>
val productos = productosDto.toDomainList()  // List<Producto>
```

### ✅ Verificación

- [ ] Mappers creados en `data/mapper/SaborLocalMappers.kt`
- [ ] Extension functions para DTOs
- [ ] mapNotNull usado para filtrar productos sin productor

---

## Paso 7: Crear Repositorios

### ¿Qué hace un Repository?

Encapsula la lógica de acceso a datos (API, caché, etc.) y expone una interfaz limpia al ViewModel.

### Crear ProductoRepository

**Ubicación:** `repository/ProductoRepository.kt`

```kotlin
package com.example.miappmodular.repository

import android.util.Log
import com.example.miappmodular.data.mapper.toDomainList
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductoRepository {

    private val apiService = RetrofitClient.saborLocalApiService

    suspend fun getProductos(): Result<List<Producto>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProductos()

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    val productos = body.data.toDomainList()
                    Result.Success(productos)
                } else {
                    Result.Error("No se pudieron obtener los productos")
                }
            } else {
                Result.Error("Error HTTP ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al obtener productos", e)
            Result.Error("Error de red: ${e.message}", e)
        }
    }

    suspend fun createProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        unidad: String,
        stock: Int,
        productorId: String
    ): Result<Producto> = withContext(Dispatchers.IO) {
        try {
            val request = CreateProductoRequest(
                nombre, descripcion, precio, unidad, stock, productorId
            )
            val response = apiService.createProducto(request)

            if (response.isSuccessful && response.body()?.data != null) {
                val producto = response.body()!!.data!!.toDomain()
                if (producto != null) {
                    Result.Success(producto)
                } else {
                    Result.Error("Error al convertir producto")
                }
            } else {
                Result.Error("Error al crear producto")
            }
        } catch (e: Exception) {
            Result.Error("Error: ${e.message}", e)
        }
    }

    // ... más métodos (update, delete, uploadImage)
}
```

### Patrón Result

```kotlin
when (val result = repository.getProductos()) {
    is Result.Success -> {
        // Acceder a result.data
        _productos.value = result.data
    }
    is Result.Error -> {
        // Acceder a result.message
        _errorMessage.value = result.message
    }
}
```

### ✅ Verificación

- [ ] ProductoRepository creado
- [ ] Métodos usan withContext(Dispatchers.IO)
- [ ] Retornan Result<T> en lugar de valores directos
- [ ] Manejo de excepciones con try-catch

---

## Paso 8: Crear ViewModels

### ¿Qué hace un ViewModel?

- Mantiene el estado de la UI (productos, loading, errores)
- Expone Flows/LiveData para que la UI observe cambios
- Ejecuta operaciones del repository en coroutines

### Crear ProductoViewModel

**Ubicación:** `viewmodel/ProductoViewModel.kt`

```kotlin
package com.example.miappmodular.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.Result
import com.example.miappmodular.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoViewModel : ViewModel() {

    private val repository = ProductoRepository()

    // Estado de la lista de productos
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.getProductos()) {
                is Result.Success -> {
                    _productos.value = result.data
                }
                is Result.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
```

### StateFlow vs LiveData

| Aspecto | StateFlow | LiveData |
|---------|-----------|----------|
| **Null Safety** | Siempre tiene valor inicial | Puede ser null |
| **Observación** | `.collectAsState()` en Compose | `.observeAsState()` en Compose |
| **Coroutines** | Nativo | Requiere extensiones |
| **Testing** | Más fácil | Requiere mocks |

### ✅ Verificación

- [ ] ProductoViewModel creado
- [ ] Usa StateFlow para estados
- [ ] Métodos ejecutan en viewModelScope.launch
- [ ] Estados: productos, isLoading, errorMessage

---

## Paso 9: Crear Screens

### Pantalla: ProductosListScreen

**Ubicación:** `ui/screens/ProductosListScreen.kt`

```kotlin
package com.example.miappmodular.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.miappmodular.model.Producto
import com.example.miappmodular.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosListScreen(
    onProductoClick: (String) -> Unit,
    onAddClick: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Cargar productos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadProductos()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SaborLocal - Productos") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, "Agregar Producto")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadProductos() }) {
                            Text("Reintentar")
                        }
                    }
                }
                productos.isEmpty() -> {
                    Text(
                        text = "No hay productos disponibles",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(productos) { producto ->
                            ProductoCard(
                                producto = producto,
                                onClick = { onProductoClick(producto.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen del producto
            AsyncImage(
                model = producto.getThumbnailUrl(),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = producto.productor.nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = producto.getPrecioFormateado(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Stock: ${producto.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (producto.tieneStock()) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }
        }
    }
}
```

### Conceptos Clave

#### 1. collectAsState()

```kotlin
val productos by viewModel.productos.collectAsState()
```

- Convierte StateFlow en State de Compose
- La UI se recompone automáticamente cuando cambia el Flow

#### 2. LaunchedEffect

```kotlin
LaunchedEffect(Unit) {
    viewModel.loadProductos()
}
```

- Ejecuta código cuando el Composable entra en composición
- `Unit` como key significa "ejecutar solo una vez"

#### 3. AsyncImage (Coil)

```kotlin
AsyncImage(
    model = producto.getThumbnailUrl(),
    contentDescription = producto.nombre,
    modifier = Modifier.size(80.dp),
    contentScale = ContentScale.Crop
)
```

- Carga imágenes desde URL
- Maneja loading y errores automáticamente

### ✅ Verificación

- [ ] ProductosListScreen creada
- [ ] Usa collectAsState() para observar ViewModelStateFlows
- [ ] Muestra loading, error y lista de productos
- [ ] AsyncImage carga thumbnails

---

## Paso 10: Configurar Navegación

### Actualizar AppNavigation

**Ubicación:** `ui/navigation/AppNavigation.kt`

```kotlin
package com.example.miappmodular.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.miappmodular.ui.screens.ProductosListScreen
// ... importar más screens

sealed class Screen(val route: String) {
    object ProductosList : Screen("productos_list")
    object ProductoDetail : Screen("producto_detail/{id}") {
        fun createRoute(id: String) = "producto_detail/$id"
    }
    // ... más rutas
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ProductosList.route
    ) {
        composable(Screen.ProductosList.route) {
            ProductosListScreen(
                onProductoClick = { id ->
                    navController.navigate(Screen.ProductoDetail.createRoute(id))
                },
                onAddClick = {
                    // Navegar a crear producto
                }
            )
        }

        composable(Screen.ProductoDetail.route) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            if (id != null) {
                ProductoDetailScreen(
                    productoId = id,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
```

### ✅ Verificación

- [ ] Navegación configurada con sealed class Screen
- [ ] ProductosList como startDestination
- [ ] Parámetros en rutas (producto_detail/{id})

---

## Testing y Depuración

### Iniciar el Backend

```bash
cd /Users/roberto/Documents/GitHub/saborlocal-api
npm run start:dev
```

Verificar que esté corriendo en: `http://localhost:3008`

### Probar Endpoints con Swagger

Abrir en navegador: `http://localhost:3008/api/docs`

1. **Crear un Productor:**
   - Endpoint: `POST /api/productor`
   - Body:
     ```json
     {
       "nombre": "Agrícola Valle Verde",
       "ubicacion": "Valparaíso",
       "telefono": "+56912345678",
       "email": "contacto@valleverde.cl"
     }
     ```
   - Copiar el `_id` retornado

2. **Crear un Producto:**
   - Endpoint: `POST /api/producto`
   - Body:
     ```json
     {
       "nombre": "Tomates Orgánicos",
       "descripcion": "Tomates cultivados sin pesticidas",
       "precio": 2500,
       "unidad": "kg",
       "stock": 50,
       "productor": "PEGAR_ID_DEL_PRODUCTOR_AQUI"
     }
     ```

3. **Listar Productos:**
   - Endpoint: `GET /api/producto`
   - Debería retornar el producto con el productor populated

### Probar la App Android

1. **Iniciar Emulador Android**

2. **Verificar Conectividad:**
   - Abrir navegador del emulador
   - Ir a: `http://10.0.2.2:3008/api/producto`
   - Debería mostrar el JSON de productos

3. **Correr la App:**
   ```bash
   ./gradlew installDebug
   ```

4. **Verificar Logs:**
   - Abrir Logcat en Android Studio
   - Filtrar por "ProductoRepository"
   - Deberías ver los logs HTTP de Retrofit

### Errores Comunes

#### 1. "Unable to resolve host"

**Causa:** El backend no está corriendo o la IP es incorrecta.

**Solución:**
```bash
# Verificar que el backend esté corriendo
curl http://localhost:3008/api/producto

# Si estás en dispositivo físico, usa la IP correcta:
# Windows: ipconfig
# macOS/Linux: ifconfig
```

#### 2. "No se pudieron obtener los productos"

**Causa:** El backend retorna un error o los datos no tienen el formato esperado.

**Solución:**
- Ver Logcat para el error exacto
- Verificar que el backend tenga productos creados
- Revisar que los DTOs coincidan con la respuesta del API

#### 3. "Error al convertir producto"

**Causa:** El productor no está populated en la respuesta.

**Solución:**
- Verificar que el backend esté usando `.populate()` en el servicio
- Revisar RELACIONES_POPULATES_COMPLETADO.md

#### 4. Imágenes no cargan

**Causa:** La URL de la imagen es incorrecta.

**Solución:**
- Verificar que `getImagenUrl()` retorne la URL completa
- Ejemplo: `http://10.0.2.2:3008/uploads/imagen.jpg`
- Agregar permisos de internet en AndroidManifest.xml:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  ```

---

## Ejercicios para Estudiantes

### Ejercicio 1: Pantalla de Detalle de Producto

**Objetivo:** Crear ProductoDetailScreen que muestre:
- Imagen grande del producto
- Nombre, descripción, precio
- Información del productor
- Stock disponible
- Botón para editar

**Pistas:**
```kotlin
@Composable
fun ProductoDetailScreen(
    productoId: String,
    viewModel: ProductoViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val producto by viewModel.productoSeleccionado.collectAsState()

    LaunchedEffect(productoId) {
        viewModel.loadProducto(productoId)
    }

    // ... UI
}
```

### Ejercicio 2: Búsqueda de Productos

**Objetivo:** Agregar un campo de búsqueda en ProductosListScreen.

**Pistas:**
- Agregar un TextField en el TopAppBar
- Usar `viewModel.searchProductos(query)`
- Filtrar productos en tiempo real

### Ejercicio 3: Subir Imagen de Producto

**Objetivo:** Implementar la funcionalidad de upload de imagen.

**Pistas:**
- Usar `ImagePickerDialog` existente
- Llamar a `viewModel.uploadImage(id, file)`
- Mostrar preview de la imagen

### Ejercicio 4: Formulario de Crear Producto

**Objetivo:** Pantalla para crear un nuevo producto.

**Campos necesarios:**
- Nombre
- Descripción
- Precio
- Unidad (dropdown: kg, litro, unidad)
- Stock
- Productor (dropdown con lista de productores)

---

## Recursos Adicionales

### Documentación

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [StateFlow Guide](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- [Coil Image Loading](https://coil-kt.github.io/coil/)

### Swagger del Backend

- **URL:** `http://localhost:3008/api/docs`
- Puedes probar todos los endpoints directamente desde el navegador

### Postman Collection

- **Ubicación:** `/Users/roberto/Documents/GitHub/saborlocal-api/saborlocal-api.postman_collection.json`
- Importar en Postman para probar el API

---

## Próximos Pasos

1. **Implementar Caché Local**
   - Usar Room para cache offline
   - Sincronizar con backend cuando haya conexión

2. **Agregar Autenticación**
   - Login/Register con JWT
   - Interceptor para agregar token a requests

3. **Gestión de Pedidos**
   - Carrito de compras
   - Crear pedidos
   - Ver historial

4. **Notificaciones Push**
   - Firebase Cloud Messaging
   - Notificar cuando cambien pedidos

5. **Modo Offline**
   - Detectar conexión
   - Queue de operaciones pendientes
   - Sincronización automática

---

## Conclusión

¡Felicitaciones! Has migrado exitosamente tu app Android de Room Database a un backend completo con NestJS + MongoDB.

**Has aprendido:**
- ✅ Arquitectura MVVM con backend
- ✅ Retrofit para consumir APIs REST
- ✅ Manejo de estados con StateFlow
- ✅ Separación de capas (DTOs, Models, Mappers, Repositories)
- ✅ Jetpack Compose con datos remotos
- ✅ Upload de imágenes multipart/form-data

**Próximo nivel:**
- 🚀 Implementar las pantallas faltantes
- 🚀 Agregar gestión de pedidos
- 🚀 Implementar autenticación
- 🚀 Deploy a producción (backend en Render/Railway, app en Google Play)

---

**Documentos relacionados:**
- `RELACIONES_POPULATES_COMPLETADO.md` - Relaciones del backend
- `SISTEMA_UPLOAD_IMAGENES.md` - Sistema de upload
- `RESUMEN_FINAL_IMPLEMENTACION.md` - Overview completo del backend

**¡Éxito en tu proyecto! 🌱**
