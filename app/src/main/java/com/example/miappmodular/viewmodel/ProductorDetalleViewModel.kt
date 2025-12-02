package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.data.local.CarritoManager
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.repository.ProductoRepository
import com.example.miappmodular.repository.ProductorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
/**
 * ViewModel para mostrar el catálogo de productos de un productor específico
 *
 * **Flujo del cliente:**
 * 1. Cliente ve lista de productores
 * 2. Click en un productor → Este ViewModel
 * 3. Muestra productos de ese productor
 * 4. Cliente puede agregar productos al carrito
 * **Arquitectura simple:**
 * - ProductoRepository para obtener productos
 * - ProductorRepository para obtener datos del productor
 * - CarritoManager para gestionar el carrito
 */
class ProductorDetalleViewModel(application: Application) : AndroidViewModel(application) {
    private val productoRepository = ProductoRepository()
    private val productorRepository = ProductorRepository()
    // Estado de la UI
    private val _uiState = MutableStateFlow<ProductorDetalleUiState>(ProductorDetalleUiState.Loading)
    val uiState: StateFlow<ProductorDetalleUiState> = _uiState.asStateFlow()
    // Productos del productor
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()
    // Información del productor
    private val _productorNombre = MutableStateFlow("")
    val productorNombre: StateFlow<String> = _productorNombre.asStateFlow()
    private val _productorId = MutableStateFlow("")

    /**
     * Carga los productos de un productor específico
     *
     * **Actualizado para usar el endpoint correcto:**
     * GET /api/producto/productor/{productorId}
     * Este endpoint retorna directamente los productos del productor,
     * sin necesidad de filtrar manualmente.
     *
     * @param productorId ID del productor
     */
    fun loadProductos(productorId: String) {
        _productorId.value = productorId
        _uiState.value = ProductorDetalleUiState.Loading
        viewModelScope.launch {
            // Usar el endpoint específico para obtener productos del productor
            val result = productoRepository.getProductosByProductor(productorId)
            when (result) {
                is com.example.miappmodular.model.ApiResult.Success -> {
                    val productosDelProductor = result.data
                    _productos.value = productosDelProductor
                    // Obtener nombre del productor desde ProductorRepository si es necesario
                    // Por ahora, obtenemos el nombre del primer producto
                    if (productosDelProductor.isNotEmpty()) {
                        // Cargar nombre del productor usando ProductorRepository
                        val productorResult = productorRepository.getProductor(productorId)
                        if (productorResult is com.example.miappmodular.model.ApiResult.Success) {
                            _productorNombre.value = productorResult.data.getDisplayName()
                        }
                    }
                    _uiState.value = if (productosDelProductor.isEmpty()) {
                        ProductorDetalleUiState.Empty
                    } else {
                        ProductorDetalleUiState.Success(productosDelProductor)
                    }
                }
                is com.example.miappmodular.model.ApiResult.Error -> {
                    _uiState.value = ProductorDetalleUiState.Error(
                        result.message
                    )
                }
            }
        }
    }

    /**
     * Agrega un producto al carrito
     * @param producto Producto a agregar
     * @param cantidad Cantidad a agregar (por defecto 1)
     */
    fun agregarAlCarrito(producto: Producto, cantidad: Int = 1) {
        if (cantidad <= 0) return
        if (cantidad > producto.stock) {
            // TODO: Mostrar mensaje de stock insuficiente
            return
        }
        CarritoManager.addItem(producto, cantidad)
    }

    /**
     * Verifica cuántos items de un producto hay en el carrito
     * @param productoId ID del producto
     * @return Cantidad en el carrito
     */
    fun getCantidadEnCarrito(productoId: String): Int {
        return CarritoManager.getCantidadProducto(productoId)
    }
}

/**
 * Estados posibles de la UI
 */
sealed class ProductorDetalleUiState {
    object Loading : ProductorDetalleUiState()
    object Empty : ProductorDetalleUiState()
    data class Success(val productos: List<Producto>) : ProductorDetalleUiState()
    data class Error(val message: String) : ProductorDetalleUiState()
}
