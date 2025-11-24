package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.Result
import com.example.miappmodular.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para crear productos
 * Solo PRODUCTORES pueden crear productos
 */
class CreateProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductoRepository()

    // Estado de la UI
    private val _uiState = MutableStateFlow<CreateProductoUiState>(CreateProductoUiState.Idle)
    val uiState: StateFlow<CreateProductoUiState> = _uiState.asStateFlow()

    // Campos del formulario
    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _descripcion = MutableStateFlow("")
    val descripcion: StateFlow<String> = _descripcion.asStateFlow()

    private val _precio = MutableStateFlow("")
    val precio: StateFlow<String> = _precio.asStateFlow()

    private val _unidad = MutableStateFlow("")
    val unidad: StateFlow<String> = _unidad.asStateFlow()

    private val _stock = MutableStateFlow("")
    val stock: StateFlow<String> = _stock.asStateFlow()

    private val _productorId = MutableStateFlow("")
    val productorId: StateFlow<String> = _productorId.asStateFlow()

    /**
     * Actualiza el nombre
     */
    fun onNombreChange(newNombre: String) {
        _nombre.value = newNombre
    }

    /**
     * Actualiza la descripción
     */
    fun onDescripcionChange(newDescripcion: String) {
        _descripcion.value = newDescripcion
    }

    /**
     * Actualiza el precio
     */
    fun onPrecioChange(newPrecio: String) {
        _precio.value = newPrecio
    }

    /**
     * Actualiza la unidad
     */
    fun onUnidadChange(newUnidad: String) {
        _unidad.value = newUnidad
    }

    /**
     * Actualiza el stock
     */
    fun onStockChange(newStock: String) {
        _stock.value = newStock
    }

    /**
     * Actualiza el ID del productor
     */
    fun onProductorIdChange(newProductorId: String) {
        _productorId.value = newProductorId
    }

    /**
     * Crea un nuevo producto
     */
    fun createProducto() {
        // Validar campos obligatorios
        if (_nombre.value.isBlank()) {
            _uiState.value = CreateProductoUiState.Error("El nombre es obligatorio")
            return
        }

        if (_descripcion.value.isBlank()) {
            _uiState.value = CreateProductoUiState.Error("La descripción es obligatoria")
            return
        }

        val precioValue = _precio.value.toDoubleOrNull()
        if (precioValue == null || precioValue <= 0) {
            _uiState.value = CreateProductoUiState.Error("El precio debe ser un número positivo")
            return
        }

        if (_unidad.value.isBlank()) {
            _uiState.value = CreateProductoUiState.Error("La unidad es obligatoria")
            return
        }

        val stockValue = _stock.value.toIntOrNull()
        if (stockValue == null || stockValue < 0) {
            _uiState.value = CreateProductoUiState.Error("El stock debe ser un número positivo o cero")
            return
        }

        if (_productorId.value.isBlank()) {
            _uiState.value = CreateProductoUiState.Error("Debe seleccionar un productor")
            return
        }

        _uiState.value = CreateProductoUiState.Loading

        viewModelScope.launch {
            val result = repository.createProducto(
                nombre = _nombre.value,
                descripcion = _descripcion.value,
                precio = precioValue,
                unidad = _unidad.value,
                stock = stockValue,
                productorId = _productorId.value
            )

            _uiState.value = when (result) {
                is Result.Success -> CreateProductoUiState.Success(result.data)
                is Result.Error -> CreateProductoUiState.Error(result.message)
            }
        }
    }

    /**
     * Resetea el estado a Idle
     */
    fun resetState() {
        _uiState.value = CreateProductoUiState.Idle
    }

    /**
     * Limpia el formulario
     */
    fun clearForm() {
        _nombre.value = ""
        _descripcion.value = ""
        _precio.value = ""
        _unidad.value = ""
        _stock.value = ""
        _productorId.value = ""
    }
}

/**
 * Estados posibles de la UI de creación de productos
 */
sealed class CreateProductoUiState {
    object Idle : CreateProductoUiState()
    object Loading : CreateProductoUiState()
    data class Success(val producto: Producto) : CreateProductoUiState()
    data class Error(val message: String) : CreateProductoUiState()
}
