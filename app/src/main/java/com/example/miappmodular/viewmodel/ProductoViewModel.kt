package com.example.miappmodular.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel para gestionar el estado y lógica de negocio de Productos
 *
 * Responsabilidades:
 * - Mantener el estado de la UI (lista de productos, producto seleccionado, etc.)
 * - Exponer Flows para que la UI observe cambios
 * - Ejecutar operaciones del repositorio en coroutines
 * - Manejar loading states y errores
 *
 * **Arquitectura simple:**
 * El ViewModel crea su propio repository directamente.
 */
class ProductoViewModel : ViewModel() {

    // El repository se crea directamente (sin inyección de dependencias)
    private val repository = ProductoRepository()

    // Estado de la lista de productos
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    // Estado del producto seleccionado
    private val _productoSeleccionado = MutableStateFlow<Producto?>(null)
    val productoSeleccionado: StateFlow<Producto?> = _productoSeleccionado.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Estado de éxito para operaciones
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    /**
    /**
     * Carga la lista de todos los productos
     */
     */
    fun loadProductos() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.getProductos()) {
                is ApiResult.Success -> {
                    _productos.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    /**
    /**
     * Carga un producto específico por ID
     */
     */
    fun loadProducto(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.getProducto(id)) {
                is ApiResult.Success -> {
                    _productoSeleccionado.value = result.data
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    /**
    /**
     * Crea un nuevo producto
     */
     */
    fun createProducto(
        nombre: String,
        descripcion: String,
        precio: Double,
        unidad: String,
        stock: Int,
        productorId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.createProducto(
                nombre, descripcion, precio, unidad, stock, productorId
            )) {
                is ApiResult.Success -> {
                    _successMessage.value = "Producto creado exitosamente"
                    loadProductos() // Recargar lista
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    /**
    /**
     * Actualiza un producto existente
     */
     */
    fun updateProducto(
        id: String,
        nombre: String? = null,
        descripcion: String? = null,
        precio: Double? = null,
        stock: Int? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.updateProducto(id, nombre, descripcion, precio, stock)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Producto actualizado exitosamente"
                    loadProductos() // Recargar lista
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    /**
    /**
     * Elimina un producto
     */
     */
    fun deleteProducto(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (repository.deleteProducto(id)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Producto eliminado exitosamente"
                    loadProductos() // Recargar lista
                }
                is ApiResult.Error -> {
                    _errorMessage.value = "Error al eliminar producto"
                }
            }

            _isLoading.value = false
        }
    }

    /**
    /**
     * Sube una imagen para un producto
     */
     */
    fun uploadImage(id: String, imageFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = repository.uploadImage(id, imageFile)) {
                is ApiResult.Success -> {
                    _successMessage.value = "Imagen subida exitosamente"
                    _productoSeleccionado.value = result.data
                    loadProductos() // Recargar lista
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    /**
    /**
     * Limpia el mensaje de error
     */
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    /**
    /**
     * Limpia el mensaje de éxito
     */
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
    /**
     * Filtra productos por nombre
     */
     */
    fun searchProductos(query: String): List<Producto> {
        return if (query.isBlank()) {
            _productos.value
        } else {
            _productos.value.filter {
                it.nombre.contains(query, ignoreCase = true) ||
                it.descripcion.contains(query, ignoreCase = true)
            }
        }
    }

    /**
    /**
     * Filtra productos por productor
     */
     */
    fun filterByProductor(productorId: String): List<Producto> {
        return _productos.value.filter { it.productor.id == productorId }
    }
}
