# 📚 Tutorial Completo: Room Database con Relaciones en Android

## 🎯 Objetivo del Tutorial

Aprenderás a implementar Room Database desde cero en una aplicación Android, incluyendo:
- ✅ Configuración de dependencias
- ✅ Creación de entidades con relaciones
- ✅ DAOs (Data Access Objects)
- ✅ Repositorios
- ✅ ViewModels
- ✅ UI con Jetpack Compose
- ✅ Gestión completa de categorías y productos

---

## 📋 Tabla de Contenidos

1. [Configuración Inicial](#1-configuración-inicial)
2. [Entender Room Database](#2-entender-room-database)
3. [Crear las Entidades](#3-crear-las-entidades)
4. [Crear los DAOs](#4-crear-los-daos)
5. [Configurar la Base de Datos](#5-configurar-la-base-de-datos)
6. [Implementar Repositorios](#6-implementar-repositorios)
7. [Crear ViewModels](#7-crear-viewmodels)
8. [Diseñar la UI con Compose](#8-diseñar-la-ui-con-compose)
9. [Casos de Uso Prácticos](#9-casos-de-uso-prácticos)
10. [Testing](#10-testing)
11. [Mejores Prácticas](#11-mejores-prácticas)

---

## 1. Configuración Inicial

### 1.1. Dependencias en `build.gradle.kts` (Module: app)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" // KSP para Room
}

dependencies {
    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Soporte para corrutinas y Flow
    ksp("androidx.room:room-compiler:$roomVersion") // Procesador de anotaciones

    // Corrutinas
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel y Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Testing
    testImplementation("androidx.room:room-testing:$roomVersion")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}
```

**💡 Explicación:**
- `room-runtime`: Biblioteca principal de Room
- `room-ktx`: Extensiones para usar Flow y corrutinas
- `room-compiler`: Genera código automáticamente (DAOs, Database)
- `ksp`: Kotlin Symbol Processing, reemplazo moderno de kapt

### 1.2. Sincronizar el proyecto

```bash
./gradlew build
```

---

## 2. Entender Room Database

### 2.1. ¿Qué es Room?

Room es una **biblioteca de persistencia** que proporciona una capa de abstracción sobre SQLite, facilitando:
- ✅ Acceso a base de datos en Android
- ✅ Verificación de SQL en tiempo de compilación
- ✅ Menos código boilerplate
- ✅ Integración con Flow y LiveData

### 2.2. Componentes de Room

```
┌─────────────────────────────────────────┐
│         Room Database Architecture       │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────┐    ┌──────────┐         │
│  │  Entity   │───>│  DAO     │         │
│  │ (Tabla)   │    │ (Queries)│         │
│  └───────────┘    └──────────┘         │
│        │                 │              │
│        └────┬────────────┘              │
│             ▼                           │
│      ┌─────────────┐                    │
│      │  Database   │                    │
│      │  (SQLite)   │                    │
│      └─────────────┘                    │
│                                         │
└─────────────────────────────────────────┘
```

**Componentes:**
1. **Entity**: Clase que representa una tabla
2. **DAO**: Interfaz con métodos para acceder a los datos
3. **Database**: Clase que contiene la configuración de la BD

---

## 3. Crear las Entidades

### 3.1. Entidad Category

**Ubicación:** `data/local/entity/Category.kt`

```kotlin
package com.example.miappmodular.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Entidad que representa una categoría de productos.
 *
 * @Entity: Marca esta clase como una tabla en Room Database
 * @PrimaryKey: Define el identificador único de cada fila
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // ID único generado automáticamente
    val nameCategory: String,                       // Nombre de la categoría
    val createdAt: Date = Date()                   // Fecha de creación
)
```

**📝 Notas importantes:**
- `@Entity(tableName = "categories")`: Define el nombre de la tabla SQL
- `@PrimaryKey`: Cada entidad debe tener una clave primaria
- `UUID.randomUUID()`: Genera IDs únicos automáticamente
- Room convierte automáticamente tipos Kotlin a tipos SQL

### 3.2. Entidad Product (con Foreign Key)

**Ubicación:** `data/local/entity/Product.kt`

```kotlin
package com.example.miappmodular.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

/**
 * Entidad que representa un producto.
 *
 * Relación: Un producto pertenece a UNA categoría
 *           Una categoría puede tener MUCHOS productos (1:N)
 */
@Entity(
    tableName = "products",

    // Foreign Key: Define la relación con Category
    foreignKeys = [
        ForeignKey(
            entity = Category::class,        // Tabla padre
            parentColumns = ["id"],          // Columna en Category
            childColumns = ["categoryId"],   // Columna en Product
            onDelete = ForeignKey.CASCADE,   // Si se elimina Category, eliminar Products
            onUpdate = ForeignKey.CASCADE    // Si cambia el ID de Category, actualizar categoryId
        )
    ],

    // Index: Optimiza las consultas JOIN por categoryId
    indices = [Index(value = ["categoryId"])]
)
data class Product(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productName: String,
    val categoryId: String,    // Foreign Key que referencia a categories.id
    val createdAt: Date = Date()
)
```

**📝 Conceptos clave:**

#### Foreign Key
```kotlin
foreignKeys = [ForeignKey(...)]
```
- **Propósito:** Garantiza integridad referencial
- **parentColumns**: Columna en la tabla padre (Category.id)
- **childColumns**: Columna en la tabla hija (Product.categoryId)

#### Cascade Options
```kotlin
onDelete = ForeignKey.CASCADE
```
- `CASCADE`: Al eliminar Category, elimina automáticamente sus Products
- `SET_NULL`: Al eliminar Category, pone categoryId en NULL
- `RESTRICT`: No permite eliminar Category si tiene Products
- `NO_ACTION`: No hace nada (depende de SQLite)

#### Index
```kotlin
indices = [Index(value = ["categoryId"])]
```
- Crea un índice en la columna `categoryId`
- **Beneficio:** Acelera las consultas JOIN entre products y categories

### 3.3. Clase de Relación ProductWithCategory

**Ubicación:** `data/local/entity/ProductWithCategory.kt`

```kotlin
package com.example.miappmodular.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Clase de datos que combina Product y Category en una sola consulta.
 *
 * Room ejecuta automáticamente el JOIN necesario.
 */
data class ProductWithCategory(
    // @Embedded: Incluye todos los campos de Product directamente
    @Embedded
    val product: Product,

    // @Relation: Room carga automáticamente la Category asociada
    @Relation(
        parentColumn = "categoryId",  // Columna en Product
        entityColumn = "id"            // Columna en Category
    )
    val category: Category
)
```

**📝 Cómo funciona:**

1. Room lee todos los campos de `Product`
2. Usa `product.categoryId` para buscar en la tabla `categories`
3. Encuentra la Category donde `id = categoryId`
4. Retorna un objeto con ambos datos

**Ejemplo de resultado:**
```kotlin
ProductWithCategory(
    product = Product(
        id = "abc123",
        productName = "Laptop",
        categoryId = "cat-001",
        createdAt = Date()
    ),
    category = Category(
        id = "cat-001",
        nameCategory = "Electrónica",
        createdAt = Date()
    )
)
```

### 3.4. Convertidores de Tipo (Type Converters)

Room solo entiende tipos primitivos. Para usar `Date`, necesitamos un convertidor.

**Ubicación:** `data/local/database/Converters.kt`

```kotlin
package com.example.miappmodular.data.local.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Convertidores de tipos para Room.
 *
 * Room solo almacena tipos primitivos (Int, String, Long, etc).
 * Esta clase convierte tipos complejos (Date) a tipos primitivos.
 */
class Converters {

    /**
     * Convierte un timestamp (Long) a Date.
     * Se usa al LEER de la base de datos.
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Convierte un Date a timestamp (Long).
     * Se usa al ESCRIBIR en la base de datos.
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
```

**💡 Flujo de conversión:**

```
Escritura a BD:
Date(2024-01-15) → Converters.dateToTimestamp() → 1705276800000 → SQLite

Lectura desde BD:
SQLite → 1705276800000 → Converters.fromTimestamp() → Date(2024-01-15)
```

---

## 4. Crear los DAOs

### 4.1. ¿Qué es un DAO?

**DAO (Data Access Object)**: Interfaz que define TODAS las operaciones de base de datos.

Room genera automáticamente la implementación de estos métodos en tiempo de compilación.

### 4.2. CategoryDao

**Ubicación:** `data/local/dao/CategoryDao.kt`

```kotlin
package com.example.miappmodular.data.local.dao

import androidx.room.*
import com.example.miappmodular.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones CRUD sobre categorías.
 *
 * @Dao: Marca esta interfaz como un Data Access Object
 * Room genera automáticamente la implementación
 */
@Dao
interface CategoryDao {

    // ========== QUERIES DE LECTURA ==========

    /**
     * Obtiene todas las categorías ordenadas alfabéticamente.
     *
     * @return Flow: Emite una nueva lista cada vez que cambia la tabla categories
     */
    @Query("SELECT * FROM categories ORDER BY nameCategory ASC")
    fun getAllCategories(): Flow<List<Category>>

    /**
     * Obtiene una categoría por su ID.
     */
    @Query("SELECT * FROM categories WHERE id = :categoryId")
    fun getCategoryById(categoryId: String): Flow<Category?>

    /**
     * Busca categorías por nombre (búsqueda parcial).
     *
     * LIKE: Operador SQL para búsqueda de texto
     * '%': Comodín que representa cualquier conjunto de caracteres
     */
    @Query("SELECT * FROM categories WHERE nameCategory LIKE '%' || :searchQuery || '%'")
    fun searchCategories(searchQuery: String): Flow<List<Category>>

    // ========== OPERACIONES DE ESCRITURA ==========

    /**
     * Inserta una nueva categoría.
     *
     * @Insert: Room genera automáticamente el código SQL INSERT
     * @return: El rowId de la fila insertada (Long)
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: Category): Long

    /**
     * Inserta múltiples categorías en una transacción.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategories(categories: List<Category>): List<Long>

    /**
     * Actualiza una categoría existente.
     *
     * @Update: Room busca por el @PrimaryKey y actualiza el resto de campos
     * @return: Número de filas actualizadas (1 si tuvo éxito, 0 si no encontró)
     */
    @Update
    suspend fun updateCategory(category: Category): Int

    /**
     * Elimina una categoría.
     *
     * ⚠️ CUIDADO: También eliminará todos los productos de esta categoría (CASCADE)
     */
    @Delete
    suspend fun deleteCategory(category: Category): Int

    /**
     * Elimina una categoría por su ID.
     */
    @Query("DELETE FROM categories WHERE id = :categoryId")
    suspend fun deleteCategoryById(categoryId: String): Int

    // ========== QUERIES DE UTILIDAD ==========

    /**
     * Cuenta el total de categorías.
     */
    @Query("SELECT COUNT(*) FROM categories")
    fun getCategoriesCount(): Flow<Int>
}
```

**📝 Anotaciones importantes:**

| Anotación | Propósito |
|-----------|-----------|
| `@Query("SQL")` | Ejecuta una query SQL personalizada |
| `@Insert` | Genera código para insertar filas |
| `@Update` | Genera código para actualizar filas |
| `@Delete` | Genera código para eliminar filas |
| `suspend` | Función suspendida (se ejecuta en corrutina) |
| `Flow<T>` | Stream reactivo que emite cuando hay cambios |

**💡 ¿Por qué usar Flow?**

```kotlin
// Sin Flow (debes llamar manualmente cada vez)
val categories = categoryDao.getAllCategoriesSync()

// Con Flow (se actualiza automáticamente cuando cambia la BD)
categoryDao.getAllCategories().collect { categories ->
    // Este bloque se ejecuta cada vez que hay cambios en la tabla
    updateUI(categories)
}
```

### 4.3. ProductDao

**Ubicación:** `data/local/dao/ProductDao.kt`

```kotlin
package com.example.miappmodular.data.local.dao

import androidx.room.*
import com.example.miappmodular.data.local.entity.Product
import com.example.miappmodular.data.local.entity.ProductWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ========== QUERIES BÁSICAS ==========

    @Query("SELECT * FROM products ORDER BY productName ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductById(productId: String): Flow<Product?>

    /**
     * Obtiene todos los productos de una categoría específica.
     */
    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY productName ASC")
    fun getProductsByCategory(categoryId: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE productName LIKE '%' || :searchQuery || '%'")
    fun searchProducts(searchQuery: String): Flow<List<Product>>

    // ========== QUERIES CON RELACIONES ==========

    /**
     * Obtiene productos con sus categorías completas.
     *
     * @Transaction: Garantiza que Product y Category se lean en una transacción atómica
     * Room ejecuta automáticamente el JOIN basándose en ProductWithCategory
     */
    @Transaction
    @Query("SELECT * FROM products ORDER BY productName ASC")
    fun getAllProductsWithCategories(): Flow<List<ProductWithCategory>>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId")
    fun getProductWithCategoryById(productId: String): Flow<ProductWithCategory?>

    @Transaction
    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    fun getProductsWithCategoriesByCategory(categoryId: String): Flow<List<ProductWithCategory>>

    // ========== OPERACIONES DE ESCRITURA ==========

    /**
     * Inserta un nuevo producto.
     *
     * ⚠️ IMPORTANTE: categoryId DEBE existir en la tabla categories
     * Si no existe, Room lanzará SQLiteConstraintException
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProduct(product: Product): Long

    @Insert
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product): Int

    @Delete
    suspend fun deleteProduct(product: Product): Int

    @Query("DELETE FROM products WHERE id = :productId")
    suspend fun deleteProductById(productId: String): Int

    /**
     * Elimina todos los productos de una categoría.
     */
    @Query("DELETE FROM products WHERE categoryId = :categoryId")
    suspend fun deleteProductsByCategory(categoryId: String): Int

    // ========== UTILIDADES ==========

    @Query("SELECT COUNT(*) FROM products")
    fun getProductsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE categoryId = :categoryId")
    fun getProductsCountByCategory(categoryId: String): Flow<Int>
}
```

---

## 5. Configurar la Base de Datos

### 5.1. Clase AppDatabase

**Ubicación:** `data/local/database/AppDatabase.kt`

```kotlin
package com.example.miappmodular.data.local.database

import android.content.Context
import androidx.room.*
import com.example.miappmodular.data.local.dao.CategoryDao
import com.example.miappmodular.data.local.dao.ProductDao
import com.example.miappmodular.data.local.entity.Category
import com.example.miappmodular.data.local.entity.Product

/**
 * Clase abstracta que representa la base de datos principal.
 *
 * Room genera automáticamente la implementación (AppDatabase_Impl)
 */
@Database(
    entities = [Category::class, Product::class],  // Tablas de la BD
    version = 1,                                    // Versión del schema
    exportSchema = false                            // No exportar schema JSON
)
@TypeConverters(Converters::class)  // Registrar convertidores de tipo
abstract class AppDatabase : RoomDatabase() {

    // Room genera automáticamente las implementaciones de estos DAOs
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao

    companion object {
        /**
         * Instancia singleton de la base de datos.
         * @Volatile: Garantiza visibilidad entre threads
         */
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Obtiene la instancia singleton de la base de datos (thread-safe).
         *
         * Patrón: Double-Checked Locking para thread-safety
         */
        fun getDatabase(context: Context): AppDatabase {
            // Si ya existe, retornarla
            return INSTANCE ?: synchronized(this) {
                // Si no existe, crearla dentro de un bloque sincronizado
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"  // Nombre del archivo .db
                )
                    .fallbackToDestructiveMigration()  // CUIDADO: Elimina datos al cambiar versión
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
```

**📝 Conceptos clave:**

#### Patrón Singleton
```kotlin
@Volatile private var INSTANCE: AppDatabase? = null
```
- Garantiza **UNA SOLA** instancia de la base de datos en toda la app
- `@Volatile`: Hace que los cambios en `INSTANCE` sean visibles para todos los threads
- `synchronized`: Evita condiciones de carrera al crear la instancia

#### Migración Destructiva
```kotlin
.fallbackToDestructiveMigration()
```
- **En desarrollo:** Elimina y recrea la BD al cambiar la versión
- **En producción:** ⚠️ NUNCA uses esto (perderás todos los datos del usuario)
- **Alternativa:** Implementa migraciones explícitas (ver sección 11)

---

## 6. Implementar Repositorios

### 6.1. ¿Qué es un Repository?

El **Repository** es una capa de abstracción entre la fuente de datos (Room) y el ViewModel.

**Beneficios:**
- ✅ Separa la lógica de negocio de la UI
- ✅ Centraliza el acceso a datos
- ✅ Facilita el testing (puedes mockear el Repository)
- ✅ Permite combinar múltiples fuentes de datos (local + remoto)

```
UI (Compose) → ViewModel → Repository → DAO → Database
```

### 6.2. CategoryRepository

**Ubicación:** `data/repository/CategoryRepository.kt`

```kotlin
package com.example.miappmodular.data.repository

import com.example.miappmodular.data.local.dao.CategoryDao
import com.example.miappmodular.data.local.entity.Category
import kotlinx.coroutines.flow.Flow

/**
 * Repository para gestionar las operaciones de categorías.
 *
 * Actúa como intermediario entre ViewModel y DAO.
 */
class CategoryRepository(private val categoryDao: CategoryDao) {

    // ========== LECTURA ==========

    /**
     * Obtiene todas las categorías como Flow.
     * El ViewModel puede observar este Flow y reaccionar a cambios.
     */
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoryById(id: String): Flow<Category?> =
        categoryDao.getCategoryById(id)

    fun searchCategories(query: String): Flow<List<Category>> =
        categoryDao.searchCategories(query)

    fun getCategoriesCount(): Flow<Int> =
        categoryDao.getCategoriesCount()

    // ========== ESCRITURA ==========

    /**
     * Inserta una nueva categoría.
     *
     * @return Result<Long> con el rowId o un error
     */
    suspend fun insertCategory(category: Category): Result<Long> {
        return try {
            val rowId = categoryDao.insertCategory(category)
            if (rowId > 0) {
                Result.success(rowId)
            } else {
                Result.failure(Exception("No se pudo insertar la categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(category: Category): Result<Int> {
        return try {
            val updatedRows = categoryDao.updateCategory(category)
            if (updatedRows > 0) {
                Result.success(updatedRows)
            } else {
                Result.failure(Exception("Categoría no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(category: Category): Result<Int> {
        return try {
            val deletedRows = categoryDao.deleteCategory(category)
            Result.success(deletedRows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== OPERACIONES ESPECIALES ==========

    /**
     * Verifica si una categoría tiene productos asociados.
     * Útil para mostrar advertencias antes de eliminar.
     */
    suspend fun categoryHasProducts(categoryId: String): Boolean {
        // Implementar cuando tengamos ProductRepository
        return false
    }
}
```

**📝 ¿Por qué usar Result<T>?**

```kotlin
// Sin Result (excepciones sin control)
suspend fun insertCategory(category: Category): Long {
    return categoryDao.insertCategory(category)  // Puede lanzar excepción
}

// Con Result (manejo explícito de errores)
suspend fun insertCategory(category: Category): Result<Long> {
    return try {
        Result.success(categoryDao.insertCategory(category))
    } catch (e: Exception) {
        Result.failure(e)  // Error capturado y manejado
    }
}

// Uso en ViewModel
repository.insertCategory(category)
    .onSuccess { rowId -> Log.d("Success", "Insertado con ID: $rowId") }
    .onFailure { error -> Log.e("Error", error.message) }
```

### 6.3. ProductRepository

**Ubicación:** `data/repository/ProductRepository.kt`

```kotlin
package com.example.miappmodular.data.repository

import com.example.miappmodular.data.local.dao.ProductDao
import com.example.miappmodular.data.local.entity.Product
import com.example.miappmodular.data.local.entity.ProductWithCategory
import kotlinx.coroutines.flow.Flow

class ProductRepository(private val productDao: ProductDao) {

    // ========== LECTURA BÁSICA ==========

    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    fun getProductById(id: String): Flow<Product?> =
        productDao.getProductById(id)

    fun getProductsByCategory(categoryId: String): Flow<List<Product>> =
        productDao.getProductsByCategory(categoryId)

    fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query)

    // ========== LECTURA CON RELACIONES ==========

    val allProductsWithCategories: Flow<List<ProductWithCategory>> =
        productDao.getAllProductsWithCategories()

    fun getProductWithCategoryById(id: String): Flow<ProductWithCategory?> =
        productDao.getProductWithCategoryById(id)

    fun getProductsWithCategoriesByCategory(categoryId: String): Flow<List<ProductWithCategory>> =
        productDao.getProductsWithCategoriesByCategory(categoryId)

    // ========== ESCRITURA ==========

    suspend fun insertProduct(product: Product): Result<Long> {
        return try {
            val rowId = productDao.insertProduct(product)
            if (rowId > 0) {
                Result.success(rowId)
            } else {
                Result.failure(Exception("No se pudo insertar el producto"))
            }
        } catch (e: android.database.sqlite.SQLiteConstraintException) {
            // Error de Foreign Key: categoryId no existe
            Result.failure(Exception("Categoría inválida: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(product: Product): Result<Int> {
        return try {
            val updatedRows = productDao.updateProduct(product)
            if (updatedRows > 0) {
                Result.success(updatedRows)
            } else {
                Result.failure(Exception("Producto no encontrado"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(product: Product): Result<Int> {
        return try {
            val deletedRows = productDao.deleteProduct(product)
            Result.success(deletedRows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========== UTILIDADES ==========

    fun getProductsCount(): Flow<Int> = productDao.getProductsCount()

    fun getProductsCountByCategory(categoryId: String): Flow<Int> =
        productDao.getProductsCountByCategory(categoryId)
}
```

---

## 7. Crear ViewModels

### 7.1. ¿Qué es un ViewModel?

El **ViewModel** gestiona el estado de la UI y sobrevive a cambios de configuración (rotación de pantalla).

```
UI (Compose) ←→ ViewModel ←→ Repository ←→ DAO
     ↓              ↓
  Observa       Mantiene
   State        Estado
```

### 7.2. CategoryViewModel

**Ubicación:** `presentation/viewmodel/CategoryViewModel.kt`

```kotlin
package com.example.miappmodular.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.data.local.entity.Category
import com.example.miappmodular.data.repository.CategoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

/**
 * UI State para la pantalla de categorías.
 *
 * Representa TODOS los posibles estados de la UI.
 */
data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel para gestionar categorías.
 */
class CategoryViewModel(
    private val repository: CategoryRepository
) : ViewModel() {

    // ========== UI STATE ==========

    /**
     * Estado privado (mutable) que solo el ViewModel puede modificar
     */
    private val _uiState = MutableStateFlow(CategoryUiState())

    /**
     * Estado público (inmutable) que la UI puede observar
     */
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    // ========== CARGAR DATOS ==========

    /**
     * Carga todas las categorías desde el repository.
     *
     * Flow se actualiza automáticamente cuando cambia la BD.
     */
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.allCategories
                .catch { exception ->
                    // Manejo de errores
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al cargar categorías: ${exception.message}"
                    )}
                }
                .collect { categories ->
                    // Actualizar estado con los datos
                    _uiState.update { it.copy(
                        categories = categories,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }

    // ========== OPERACIONES ==========

    /**
     * Crea una nueva categoría.
     */
    fun createCategory(name: String) {
        // Validar entrada
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val category = Category(
                nameCategory = name.trim(),
                createdAt = Date()
            )

            repository.insertCategory(category)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Categoría creada exitosamente",
                        error = null
                    )}
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al crear categoría: ${exception.message}"
                    )}
                }
        }
    }

    /**
     * Actualiza una categoría existente.
     */
    fun updateCategory(category: Category, newName: String) {
        if (newName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val updatedCategory = category.copy(nameCategory = newName.trim())

            repository.updateCategory(updatedCategory)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Categoría actualizada",
                        error = null
                    )}
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al actualizar: ${exception.message}"
                    )}
                }
        }
    }

    /**
     * Elimina una categoría.
     * ⚠️ También eliminará todos los productos asociados (CASCADE)
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.deleteCategory(category)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Categoría eliminada",
                        error = null
                    )}
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al eliminar: ${exception.message}"
                    )}
                }
        }
    }

    // ========== LIMPIEZA DE MENSAJES ==========

    /**
     * Limpia mensajes de error y éxito.
     * Útil para mostrar Snackbars que se auto-ocultan.
     */
    fun clearMessages() {
        _uiState.update { it.copy(
            error = null,
            successMessage = null
        )}
    }
}
```

**📝 Conceptos clave:**

#### StateFlow vs LiveData
```kotlin
// StateFlow (moderno, preferido)
val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

// Observar en Compose
val state by viewModel.uiState.collectAsState()

// LiveData (antiguo)
val uiState: LiveData<CategoryUiState> = _uiState.asLiveData()

// Observar en Compose
val state by viewModel.uiState.observeAsState()
```

#### viewModelScope
```kotlin
viewModelScope.launch {
    // Código de corrutina
}
```
- Scope automático vinculado al ciclo de vida del ViewModel
- Se cancela automáticamente cuando el ViewModel se destruye
- Evita memory leaks

### 7.3. ProductViewModel

**Ubicación:** `presentation/viewmodel/ProductViewModel.kt`

```kotlin
package com.example.miappmodular.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.data.local.entity.Product
import com.example.miappmodular.data.local.entity.ProductWithCategory
import com.example.miappmodular.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

data class ProductUiState(
    val products: List<ProductWithCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadProductsWithCategories()
    }

    private fun loadProductsWithCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.allProductsWithCategories
                .catch { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al cargar productos: ${exception.message}"
                    )}
                }
                .collect { products ->
                    _uiState.update { it.copy(
                        products = products,
                        isLoading = false,
                        error = null
                    )}
                }
        }
    }

    /**
     * Crea un nuevo producto.
     *
     * @param name Nombre del producto
     * @param categoryId ID de la categoría (DEBE existir)
     */
    fun createProduct(name: String, categoryId: String) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        if (categoryId.isBlank()) {
            _uiState.update { it.copy(error = "Debe seleccionar una categoría") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val product = Product(
                productName = name.trim(),
                categoryId = categoryId,
                createdAt = Date()
            )

            repository.insertProduct(product)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Producto creado exitosamente",
                        error = null
                    )}
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al crear producto"
                    )}
                }
        }
    }

    fun updateProduct(product: Product, newName: String) {
        if (newName.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val updatedProduct = product.copy(productName = newName.trim())

            repository.updateProduct(updatedProduct)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Producto actualizado",
                        error = null
                    )}
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al actualizar: ${exception.message}"
                    )}
                }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.deleteProduct(product)
                .onSuccess {
                    _uiState.update { it.copy(
                        isLoading = false,
                        successMessage = "Producto eliminado",
                        error = null
                    )}
                }
                .onFailure { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "Error al eliminar: ${exception.message}"
                    )}
                }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(
            error = null,
            successMessage = null
        )}
    }

    /**
     * Filtra productos por categoría.
     */
    fun filterByCategory(categoryId: String?) {
        viewModelScope.launch {
            if (categoryId == null) {
                // Mostrar todos
                loadProductsWithCategories()
            } else {
                // Filtrar por categoría
                repository.getProductsWithCategoriesByCategory(categoryId)
                    .collect { products ->
                        _uiState.update { it.copy(products = products) }
                    }
            }
        }
    }
}
```

---

## 8. Diseñar la UI con Compose

### 8.1. Pantalla de Categorías

**Ubicación:** `presentation/screen/CategoryScreen.kt`

```kotlin
package com.example.miappmodular.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.data.local.entity.Category
import com.example.miappmodular.presentation.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    viewModel: CategoryViewModel = viewModel()
) {
    // Observar el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Estado local para el diálogo
    var showDialog by remember { mutableStateOf(false) }

    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensajes de error o éxito
    LaunchedEffect(uiState.error, uiState.successMessage) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorías") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar categoría")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.categories.isEmpty() -> {
                    Text(
                        "No hay categorías",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.categories) { category ->
                            CategoryItem(
                                category = category,
                                onDelete = { viewModel.deleteCategory(category) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear categoría
    if (showDialog) {
        CreateCategoryDialog(
            onDismiss = { showDialog = false },
            onConfirm = { name ->
                viewModel.createCategory(name)
                showDialog = false
            }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.nameCategory,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "ID: ${category.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Categoría") },
        text = {
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text("Nombre") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(categoryName) },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
```

### 8.2. Pantalla de Productos

**Ubicación:** `presentation/screen/ProductScreen.kt`

```kotlin
package com.example.miappmodular.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miappmodular.data.local.entity.Product
import com.example.miappmodular.data.local.entity.ProductWithCategory
import com.example.miappmodular.presentation.viewmodel.ProductViewModel
import com.example.miappmodular.presentation.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productViewModel: ProductViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val productUiState by productViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(productUiState.error, productUiState.successMessage) {
        productUiState.error?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearMessages()
        }
        productUiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            productViewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                productUiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                productUiState.products.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No hay productos",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Toca + para agregar uno",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    // Agrupar productos por categoría
                    val groupedProducts = productUiState.products.groupBy { it.category }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        groupedProducts.forEach { (category, products) ->
                            // Encabezado de categoría
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = category.nameCategory,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }

                            // Productos de esta categoría
                            items(products) { productWithCategory ->
                                ProductItem(
                                    productWithCategory = productWithCategory,
                                    onDelete = {
                                        productViewModel.deleteProduct(productWithCategory.product)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        CreateProductDialog(
            categories = categoryUiState.categories,
            onDismiss = { showDialog = false },
            onConfirm = { name, categoryId ->
                productViewModel.createProduct(name, categoryId)
                showDialog = false
            }
        )
    }
}

@Composable
fun ProductItem(
    productWithCategory: ProductWithCategory,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = productWithCategory.product.productName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Categoría: ${productWithCategory.category.nameCategory}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProductDialog(
    categories: List<com.example.miappmodular.data.local.entity.Category>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var productName by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Producto") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    label = { Text("Nombre del producto") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown para seleccionar categoría
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.nameCategory ?: "Seleccionar categoría",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.nameCategory) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                if (categories.isEmpty()) {
                    Text(
                        "⚠️ Primero crea una categoría",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(productName, selectedCategoryId) },
                enabled = productName.isNotBlank() && selectedCategoryId.isNotEmpty()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
```

---

## 9. Casos de Uso Prácticos

### 9.1. Inicializar la base de datos con datos de prueba

```kotlin
// En tu Activity o Application
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar BD con datos de prueba
        lifecycleScope.launch {
            seedDatabaseIfEmpty()
        }

        setContent {
            // Tu UI
        }
    }

    private suspend fun seedDatabaseIfEmpty() {
        val database = AppDatabase.getDatabase(applicationContext)
        val categoryDao = database.categoryDao()
        val productDao = database.productDao()

        // Verificar si ya hay datos
        val count = categoryDao.getCategoriesCount().first()
        if (count > 0) return // Ya tiene datos

        // Crear categorías
        val electronics = Category(nameCategory = "Electrónica")
        val clothing = Category(nameCategory = "Ropa")
        val food = Category(nameCategory = "Alimentos")

        categoryDao.insertCategories(listOf(electronics, clothing, food))

        // Crear productos
        val products = listOf(
            Product(productName = "Laptop HP", categoryId = electronics.id),
            Product(productName = "Mouse Logitech", categoryId = electronics.id),
            Product(productName = "Teclado Mecánico", categoryId = electronics.id),
            Product(productName = "Camiseta Polo", categoryId = clothing.id),
            Product(productName = "Pantalón Jeans", categoryId = clothing.id),
            Product(productName = "Manzanas Rojas", categoryId = food.id),
            Product(productName = "Pan Integral", categoryId = food.id)
        )

        productDao.insertProducts(products)
    }
}
```

### 9.2. Búsqueda en tiempo real

```kotlin
@Composable
fun SearchableProductList(viewModel: ProductViewModel) {
    var searchQuery by remember { mutableStateOf("") }

    Column {
        // Barra de búsqueda
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                // Buscar automáticamente
                viewModel.searchProducts(query)
            },
            label = { Text("Buscar productos") },
            modifier = Modifier.fillMaxWidth()
        )

        // Lista de resultados
        val products by viewModel.searchResults.collectAsState()

        LazyColumn {
            items(products) { product ->
                ProductItem(product)
            }
        }
    }
}
```

### 9.3. Confirmación antes de eliminar (evitar eliminación accidental)

```kotlin
@Composable
fun CategoryItemWithConfirmation(
    category: Category,
    onDelete: () -> Unit
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    Card {
        Row {
            Text(category.nameCategory)

            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar eliminación") },
            text = {
                Text("¿Estás seguro de eliminar '${category.nameCategory}'?\n\n" +
                     "⚠️ Esto también eliminará todos los productos de esta categoría.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showConfirmDialog = false
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
```

---

## 10. Testing

### 10.1. Test Unitario para DAO

```kotlin
@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        // Crear base de datos en memoria (no persiste)
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Solo para testing
            .build()

        productDao = database.productDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertProduct_retrievesCorrectly() = runBlocking {
        // Crear categoría
        val category = Category(nameCategory = "Test Category")
        categoryDao.insertCategory(category)

        // Crear producto
        val product = Product(
            productName = "Test Product",
            categoryId = category.id
        )
        productDao.insertProduct(product)

        // Verificar
        val retrieved = productDao.getProductById(product.id).first()
        assertEquals(product.productName, retrieved?.productName)
        assertEquals(category.id, retrieved?.categoryId)
    }

    @Test
    fun deleteCategory_cascadeDeletesProducts() = runBlocking {
        // Crear categoría con productos
        val category = Category(nameCategory = "To Delete")
        categoryDao.insertCategory(category)

        val product1 = Product(productName = "Product 1", categoryId = category.id)
        val product2 = Product(productName = "Product 2", categoryId = category.id)
        productDao.insertProducts(listOf(product1, product2))

        // Verificar que hay 2 productos
        val beforeDelete = productDao.getAllProducts().first()
        assertEquals(2, beforeDelete.size)

        // Eliminar categoría
        categoryDao.deleteCategory(category)

        // Verificar que los productos se eliminaron (CASCADE)
        val afterDelete = productDao.getAllProducts().first()
        assertEquals(0, afterDelete.size)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertProductWithInvalidCategory_throwsException() = runBlocking {
        // Intentar insertar producto con categoryId que no existe
        val product = Product(
            productName = "Invalid Product",
            categoryId = "nonexistent-category-id"
        )
        productDao.insertProduct(product) // Debería lanzar excepción
    }

    @Test
    fun getProductsWithCategories_returnsCorrectData() = runBlocking {
        // Crear categoría
        val category = Category(nameCategory = "Electronics")
        categoryDao.insertCategory(category)

        // Crear producto
        val product = Product(productName = "Laptop", categoryId = category.id)
        productDao.insertProduct(product)

        // Obtener con relación
        val result = productDao.getAllProductsWithCategories().first()

        assertEquals(1, result.size)
        assertEquals("Laptop", result[0].product.productName)
        assertEquals("Electronics", result[0].category.nameCategory)
    }
}
```

---

## 11. Mejores Prácticas

### 11.1. Migraciones en Producción (Preservar Datos)

⚠️ **NUNCA** uses `.fallbackToDestructiveMigration()` en producción.

```kotlin
// Migración de versión 1 a 2
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Crear tabla categories
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS categories (
                id TEXT NOT NULL PRIMARY KEY,
                nameCategory TEXT NOT NULL,
                createdAt INTEGER NOT NULL
            )
        """)

        // Crear tabla products con FK
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS products (
                id TEXT NOT NULL PRIMARY KEY,
                productName TEXT NOT NULL,
                categoryId TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(categoryId) REFERENCES categories(id)
                    ON UPDATE CASCADE ON DELETE CASCADE
            )
        """)

        // Crear índice
        database.execSQL("""
            CREATE INDEX index_products_categoryId
            ON products (categoryId)
        """)
    }
}

// Registrar migración
fun getDatabase(context: Context): AppDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "app_database"
    )
        .addMigrations(MIGRATION_1_2) // Agregar migración
        .build()
}
```

### 11.2. Validación de Entrada

```kotlin
fun createProduct(name: String, categoryId: String) {
    // Validar nombre
    if (name.isBlank()) {
        _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
        return
    }

    // Validar longitud
    if (name.length > 100) {
        _uiState.update { it.copy(error = "El nombre es demasiado largo") }
        return
    }

    // Validar caracteres especiales
    if (!name.matches(Regex("^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]+$"))) {
        _uiState.update { it.copy(error = "Caracteres inválidos en el nombre") }
        return
    }

    // Validar categoría
    if (categoryId.isBlank()) {
        _uiState.update { it.copy(error = "Debe seleccionar una categoría") }
        return
    }

    // Proceder con la creación...
}
```

### 11.3. Manejo de Errores Robusto

```kotlin
suspend fun insertProduct(product: Product): Result<Long> {
    return try {
        val rowId = productDao.insertProduct(product)
        Result.success(rowId)
    } catch (e: SQLiteConstraintException) {
        // Foreign Key violation
        if (e.message?.contains("FOREIGN KEY") == true) {
            Result.failure(Exception("La categoría seleccionada no existe"))
        } else {
            Result.failure(Exception("Producto duplicado"))
        }
    } catch (e: SQLiteException) {
        // Otros errores de BD
        Result.failure(Exception("Error de base de datos: ${e.message}"))
    } catch (e: Exception) {
        // Errores generales
        Result.failure(e)
    }
}
```

### 11.4. Optimización de Consultas

```kotlin
// ❌ MAL: Cargar todos los productos y filtrar en memoria
val allProducts = productDao.getAllProducts().first()
val filtered = allProducts.filter { it.categoryId == categoryId }

// ✅ BIEN: Filtrar en la base de datos (más rápido)
val filtered = productDao.getProductsByCategory(categoryId).first()
```

### 11.5. Usar Indices para Mejorar Rendimiento

```kotlin
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["categoryId"]),           // Para JOIN rápido
        Index(value = ["productName"]),          // Para búsqueda por nombre
        Index(value = ["createdAt"]),            // Para ordenar por fecha
        Index(value = ["categoryId", "createdAt"]) // Índice compuesto
    ]
)
data class Product(...)
```

---

## 12. Resumen del Flujo Completo

```
┌─────────────────────────────────────────────────────────┐
│                  FLUJO DE DATOS EN ROOM                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  1. Usuario interactúa con la UI (Compose)              │
│     ↓                                                   │
│  2. UI llama a función del ViewModel                    │
│     ↓                                                   │
│  3. ViewModel llama a función del Repository            │
│     ↓                                                   │
│  4. Repository llama a función del DAO                  │
│     ↓                                                   │
│  5. DAO ejecuta SQL en la base de datos (Room)          │
│     ↓                                                   │
│  6. Room retorna Flow<Data> al DAO                      │
│     ↓                                                   │
│  7. DAO retorna Flow al Repository                      │
│     ↓                                                   │
│  8. Repository retorna Flow al ViewModel                │
│     ↓                                                   │
│  9. ViewModel actualiza StateFlow<UiState>              │
│     ↓                                                   │
│ 10. UI observa el StateFlow y se re-compone             │
│     ↓                                                   │
│ 11. Usuario ve los cambios en pantalla                  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 13. Checklist de Implementación

- [ ] **Configuración**
  - [ ] Agregar dependencias de Room en `build.gradle.kts`
  - [ ] Agregar plugin KSP
  - [ ] Sincronizar proyecto

- [ ] **Entidades**
  - [ ] Crear `Category.kt` con `@Entity`
  - [ ] Crear `Product.kt` con `@ForeignKey`
  - [ ] Crear `ProductWithCategory.kt` con `@Relation`
  - [ ] Crear `Converters.kt` para tipos Date

- [ ] **DAOs**
  - [ ] Crear `CategoryDao.kt` con operaciones CRUD
  - [ ] Crear `ProductDao.kt` con queries de relación

- [ ] **Base de Datos**
  - [ ] Crear `AppDatabase.kt` con patrón Singleton
  - [ ] Registrar entidades en `@Database`
  - [ ] Registrar convertidores con `@TypeConverters`

- [ ] **Repositorios**
  - [ ] Crear `CategoryRepository.kt`
  - [ ] Crear `ProductRepository.kt`

- [ ] **ViewModels**
  - [ ] Crear `CategoryViewModel.kt` con `UiState`
  - [ ] Crear `ProductViewModel.kt` con `UiState`

- [ ] **UI**
  - [ ] Crear `CategoryScreen.kt` con Compose
  - [ ] Crear `ProductScreen.kt` con Compose
  - [ ] Implementar diálogos de creación/edición

- [ ] **Testing**
  - [ ] Crear tests unitarios para DAOs
  - [ ] Crear tests para Repositories
  - [ ] Crear tests de integración

- [ ] **Producción**
  - [ ] Implementar migraciones explícitas
  - [ ] Agregar validación de datos
  - [ ] Implementar manejo robusto de errores
  - [ ] Agregar logging para debugging

---

## 14. Recursos Adicionales para Estudiantes

### Documentación Oficial
- [Room Database - Android Developers](https://developer.android.com/training/data-storage/room)
- [Database Relations](https://developer.android.com/training/data-storage/room/relationships)
- [Testing Room](https://developer.android.com/training/data-storage/room/testing-db)

### Conceptos Avanzados
- [Database Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [Full-Text Search in Room](https://developer.android.com/training/data-storage/room/defining-data#fts)
- [Multi-module with Room](https://developer.android.com/training/data-storage/room/prepopulate)

### Ejercicios Prácticos
1. Agregar campo `price: Double` a Product
2. Implementar búsqueda por rango de precios
3. Crear relación many-to-many (Tags en productos)
4. Implementar paginación con Paging 3
5. Agregar ordenamiento personalizado (alfabético, fecha, precio)

---

**¡Fin del Tutorial!** 🎉

Ahora tus alumnos tienen una guía completa desde la configuración inicial hasta la implementación de una aplicación funcional con Room Database, relaciones, y UI moderna con Jetpack Compose.
