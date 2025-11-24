package com.example.miappmodular.data.local

/**
 * ARCHIVO DE EJEMPLO - NO ES CÓDIGO EJECUTABLE
 *
 * Este archivo muestra ejemplos de cómo usar las relaciones entre Product y Category
 * en diferentes capas de tu aplicación (Repository, ViewModel, UI).
 */

/*
// ========================================================================
// EJEMPLO 1: Repository Layer - Operaciones básicas
// ========================================================================

class ProductRepository(private val database: AppDatabase) {
    private val productDao = database.productDao()
    private val categoryDao = database.categoryDao()

    // Obtener todos los productos con sus categorías
    fun getAllProductsWithCategories(): Flow<List<ProductWithCategory>> {
        return productDao.getAllProductsWithCategories()
    }

    // Insertar una nueva categoría
    suspend fun createCategory(name: String): Result<Category> {
        return try {
            val category = Category(
                nameCategory = name,
                createdAt = Date()
            )
            val rowId = categoryDao.insertCategory(category)
            if (rowId > 0) {
                Result.success(category)
            } else {
                Result.failure(Exception("No se pudo insertar la categoría"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Insertar un producto (requiere categoryId válido)
    suspend fun createProduct(name: String, categoryId: String): Result<Product> {
        return try {
            val product = Product(
                productName = name,
                categoryId = categoryId,
                createdAt = Date()
            )
            val rowId = productDao.insertProduct(product)
            if (rowId > 0) {
                Result.success(product)
            } else {
                Result.failure(Exception("No se pudo insertar el producto"))
            }
        } catch (e: Exception) {
            // Si el categoryId no existe, Room lanzará SQLiteConstraintException
            Result.failure(e)
        }
    }

    // Obtener productos de una categoría específica
    fun getProductsByCategory(categoryId: String): Flow<List<Product>> {
        return productDao.getProductsByCategory(categoryId)
    }

    // Búsqueda de productos con categorías
    fun searchProducts(query: String): Flow<List<ProductWithCategory>> {
        return productDao.searchProductsWithCategories(query)
    }

    // Eliminar una categoría (también eliminará sus productos por CASCADE)
    suspend fun deleteCategory(category: Category): Result<Int> {
        return try {
            val deletedRows = categoryDao.deleteCategory(category)
            Result.success(deletedRows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// ========================================================================
// EJEMPLO 2: ViewModel - Manejo de UI State
// ========================================================================

data class ProductUiState(
    val products: List<ProductWithCategory> = emptyList(),
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductUiState())
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Cargar productos con categorías
                repository.getAllProductsWithCategories()
                    .collect { products ->
                        _uiState.update { it.copy(
                            products = products,
                            isLoading = false,
                            error = null
                        )}
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                )}
            }
        }
    }

    fun createProduct(name: String, categoryId: String) {
        viewModelScope.launch {
            repository.createProduct(name, categoryId)
                .onSuccess {
                    // El Flow se actualizará automáticamente
                    Log.d("ProductViewModel", "Producto creado: $name")
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
                .onSuccess { deletedCount ->
                    Log.d("ProductViewModel", "Categoría eliminada con $deletedCount productos")
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            repository.searchProducts(query)
                .collect { results ->
                    _uiState.update { it.copy(products = results) }
                }
        }
    }
}

// ========================================================================
// EJEMPLO 3: Jetpack Compose UI - Mostrar productos con categorías
// ========================================================================

@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Productos") })
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Agrupar productos por categoría
                    val groupedProducts = uiState.products.groupBy { it.category }

                    groupedProducts.forEach { (category, products) ->
                        // Encabezado de categoría
                        item {
                            CategoryHeader(category = category)
                        }

                        // Productos de esta categoría
                        items(products) { productWithCategory ->
                            ProductItem(productWithCategory = productWithCategory)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(category: Category) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = category.nameCategory,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun ProductItem(productWithCategory: ProductWithCategory) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = productWithCategory.product.productName,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Categoría: ${productWithCategory.category.nameCategory}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ========================================================================
// EJEMPLO 4: Crear datos de prueba
// ========================================================================

class DatabaseSeeder(private val database: AppDatabase) {

    suspend fun seedDatabase() {
        val categoryDao = database.categoryDao()
        val productDao = database.productDao()

        // Crear categorías
        val electronics = Category(nameCategory = "Electrónica")
        val clothing = Category(nameCategory = "Ropa")
        val food = Category(nameCategory = "Alimentos")

        categoryDao.insertCategories(listOf(electronics, clothing, food))

        // Crear productos
        val products = listOf(
            Product(productName = "Laptop", categoryId = electronics.id),
            Product(productName = "Mouse", categoryId = electronics.id),
            Product(productName = "Camiseta", categoryId = clothing.id),
            Product(productName = "Pantalón", categoryId = clothing.id),
            Product(productName = "Manzanas", categoryId = food.id),
            Product(productName = "Pan", categoryId = food.id)
        )

        productDao.insertProducts(products)
    }
}

// ========================================================================
// EJEMPLO 5: Dependency Injection (si usas Hilt)
// ========================================================================

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideCategoryDao(database: AppDatabase): CategoryDao {
        return database.categoryDao()
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    fun provideProductRepository(database: AppDatabase): ProductRepository {
        return ProductRepository(database)
    }
}

// ========================================================================
// EJEMPLO 6: Testing - Pruebas unitarias
// ========================================================================

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var productDao: ProductDao
    private lateinit var categoryDao: CategoryDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = database.productDao()
        categoryDao = database.categoryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertProductWithCategory_returnsProductWithCategory() = runBlocking {
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
        val result = productDao.getProductWithCategoryById(product.id).first()
        assertNotNull(result)
        assertEquals("Test Product", result?.product?.productName)
        assertEquals("Test Category", result?.category?.nameCategory)
    }

    @Test
    fun deleteCategory_cascadeDeletesProducts() = runBlocking {
        // Crear categoría con productos
        val category = Category(nameCategory = "To Delete")
        categoryDao.insertCategory(category)

        val product = Product(productName = "Product 1", categoryId = category.id)
        productDao.insertProduct(product)

        // Eliminar categoría
        categoryDao.deleteCategory(category)

        // Verificar que el producto también se eliminó
        val remainingProducts = productDao.getAllProducts().first()
        assertEquals(0, remainingProducts.size)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertProductWithInvalidCategoryId_throwsException() = runBlocking {
        // Intentar insertar producto con categoryId inexistente
        val product = Product(
            productName = "Invalid Product",
            categoryId = "nonexistent-id"
        )
        productDao.insertProduct(product) // Debería lanzar excepción
    }
}
*/
