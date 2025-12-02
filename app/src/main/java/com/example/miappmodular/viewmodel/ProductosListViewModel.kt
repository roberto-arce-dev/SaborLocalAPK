package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para listar productos con filtros
 *
 * **Arquitectura simple:**
 * El ViewModel crea su propio repository directamente.
 */
class ProductosListViewModel(application: Application) : AndroidViewModel(application) {

    // El repository se crea directamente (sin inyección de dependencias)
    private val repository = ProductoRepository()

    // Estado de la UI
    private val _uiState = MutableStateFlow<ProductosListUiState>(ProductosListUiState.Loading)
    val uiState: StateFlow<ProductosListUiState> = _uiState.asStateFlow()

    // Lista completa de productos (sin filtrar)
    private var allProductos: List<Producto> = emptyList()

    // Filtros
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedProductor = MutableStateFlow<String?>(null)
    val selectedProductor: StateFlow<String?> = _selectedProductor.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice: StateFlow<Double?> = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice: StateFlow<Double?> = _maxPrice.asStateFlow()

    init {
        loadProductos()
    }

    /**
    /**
     * Carga todos los productos desde el backend
     */
     */
    fun loadProductos() {
        _uiState.value = ProductosListUiState.Loading

        viewModelScope.launch {
            when (val result = repository.getProductos()) {
                is ApiResult.Success -> {
                    allProductos = result.data
                    applyFilters()
                }
                is ApiResult.Error -> {
                    _uiState.value = ProductosListUiState.Error(result.message)
                }
            }
        }
    }

    /**
    /**
     * Actualiza el query de búsqueda
     */
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
    /**
     * Filtra por productor
     */
     */
    fun onProductorFilterChange(productorId: String?) {
        _selectedProductor.value = productorId
        applyFilters()
    }

    /**
    /**
     * Filtra por precio mínimo
     */
     */
    fun onMinPriceChange(price: Double?) {
        _minPrice.value = price
        applyFilters()
    }

    /**
    /**
     * Filtra por precio máximo
     */
     */
    fun onMaxPriceChange(price: Double?) {
        _maxPrice.value = price
        applyFilters()
    }

    /**
    /**
     * Limpia todos los filtros
     */
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedProductor.value = null
        _minPrice.value = null
        _maxPrice.value = null
        applyFilters()
    }

    /**
    /**
     * Aplica todos los filtros activos
     */
     */
    private fun applyFilters() {
        var filtered = allProductos

        // Filtro por nombre (búsqueda)
        if (_searchQuery.value.isNotBlank()) {
            filtered = filtered.filter {
                it.nombre.contains(_searchQuery.value, ignoreCase = true) ||
                it.descripcion.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        // Filtro por productor
        _selectedProductor.value?.let { productorId ->
            filtered = filtered.filter { it.productor.id == productorId }
        }

        // Filtro por precio mínimo
        _minPrice.value?.let { min ->
            filtered = filtered.filter { it.precio >= min }
        }

        // Filtro por precio máximo
        _maxPrice.value?.let { max ->
            filtered = filtered.filter { it.precio <= max }
        }

        // Actualizar estado
        if (filtered.isEmpty() && allProductos.isNotEmpty()) {
            _uiState.value = ProductosListUiState.Empty("No se encontraron productos con los filtros aplicados")
        } else if (filtered.isEmpty()) {
            _uiState.value = ProductosListUiState.Empty("No hay productos disponibles")
        } else {
            _uiState.value = ProductosListUiState.Success(filtered)
        }
    }

    /**
    /**
     * Obtiene lista única de productores de los productos
     */
     */
    fun getProductores(): List<Pair<String, String>> {
        return allProductos
            .map { it.productor.id to it.productor.getDisplayName() }
            .distinctBy { it.first }
    }
}

/**
 * Estados posibles de la UI de lista de productos
 */
sealed class ProductosListUiState {
    object Loading : ProductosListUiState()
    data class Success(val productos: List<Producto>) : ProductosListUiState()
    data class Error(val message: String) : ProductosListUiState()
    data class Empty(val message: String) : ProductosListUiState()
}
