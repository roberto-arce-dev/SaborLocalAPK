package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.model.Productor
import com.example.miappmodular.repository.ProductoRepository
import com.example.miappmodular.repository.ProductorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para HomeScreen
 *
 * Gestiona la carga de datos iniciales para la pantalla de inicio:
 * - Productos destacados/recientes
 * - Productores destacados
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val productoRepository = ProductoRepository()
    private val productorRepository = ProductorRepository()

    // Estado de productos destacados (recientes)
    private val _recentProducts = MutableStateFlow<HomeUiState<List<Producto>>>(HomeUiState.Loading)
    val recentProducts: StateFlow<HomeUiState<List<Producto>>> = _recentProducts.asStateFlow()

    // Estado de productores destacados
    private val _featuredProductores = MutableStateFlow<HomeUiState<List<Productor>>>(HomeUiState.Loading)
    val featuredProductores: StateFlow<HomeUiState<List<Productor>>> = _featuredProductores.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        loadRecentProducts()
        loadFeaturedProductores()
    }

    private fun loadRecentProducts() {
        _recentProducts.value = HomeUiState.Loading
        viewModelScope.launch {
            when (val result = productoRepository.getProductos()) {
                is ApiResult.Success -> {
                    // Tomamos los primeros 10 productos como "recientes" o "destacados"
                    val products = result.data.take(10)
                    _recentProducts.value = HomeUiState.Success(products)
                }
                is ApiResult.Error -> {
                    _recentProducts.value = HomeUiState.Error(result.message)
                }
            }
        }
    }

    private fun loadFeaturedProductores() {
        _featuredProductores.value = HomeUiState.Loading
        viewModelScope.launch {
            when (val result = productorRepository.getProductores()) {
                is ApiResult.Success -> {
                    // Tomamos los primeros 5 productores como "destacados"
                    val productores = result.data.take(5)
                    _featuredProductores.value = HomeUiState.Success(productores)
                }
                is ApiResult.Error -> {
                    _featuredProductores.value = HomeUiState.Error(result.message)
                }
            }
        }
    }
}

/**
 * Estados posibles de la UI en HomeScreen
 */
sealed class HomeUiState<out T> {
    object Loading : HomeUiState<Nothing>()
    data class Success<T>(val data: T) : HomeUiState<T>()
    data class Error(val message: String) : HomeUiState<Nothing>()
}
