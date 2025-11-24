package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.User
import com.example.miappmodular.repository.AuthSaborLocalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para listar productores (Usuarios con rol PRODUCTOR)
 *
 * **Arquitectura simple:**
 * El ViewModel crea su propio repository directamente.
 */
class ProductoresListViewModel(application: Application) : AndroidViewModel(application) {

    // El repository se crea directamente (sin inyección de dependencias)
    private val repository = AuthSaborLocalRepository()

    private val _uiState = MutableStateFlow<ProductoresListUiState>(ProductoresListUiState.Loading)
    val uiState: StateFlow<ProductoresListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var allProductores: List<User> = emptyList()

    init {
        loadProductores()
    }

    /**
     * Carga la lista de usuarios PRODUCTOR
     */
    fun loadProductores() {
        _uiState.value = ProductoresListUiState.Loading

        viewModelScope.launch {
            val result = repository.getAllUsers()

            result.onSuccess { users ->
                // Filtrar solo usuarios con rol PRODUCTOR
                val productores = users.filter { it.isProductor() }

                allProductores = productores

                _uiState.value = if (productores.isEmpty()) {
                    ProductoresListUiState.Empty
                } else {
                    applyFilters()
                    ProductoresListUiState.Success(productores)
                }
            }.onFailure { error ->
                _uiState.value = ProductoresListUiState.Error(
                    error.message ?: "Error desconocido al cargar productores"
                )
            }
        }
    }

    /**
     * Actualiza el query de búsqueda
     */
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    /**
     * Aplica los filtros de búsqueda
     */
    private fun applyFilters() {
        var filtered = allProductores

        // Filtro por nombre, ubicación o email
        if (_searchQuery.value.isNotBlank()) {
            filtered = filtered.filter {
                it.nombre.contains(_searchQuery.value, ignoreCase = true) ||
                (it.ubicacion?.contains(_searchQuery.value, ignoreCase = true) ?: false) ||
                it.email.contains(_searchQuery.value, ignoreCase = true)
            }
        }

        _uiState.value = if (filtered.isEmpty()) {
            ProductoresListUiState.Empty
        } else {
            ProductoresListUiState.Success(filtered)
        }
    }

    /**
     * Elimina un productor (TODO: implementar endpoint para eliminar usuarios)
     */
    fun deleteProductor(id: String) {
        // Por ahora, simplemente mostrar error ya que no hay endpoint para eliminar usuarios
        _uiState.value = ProductoresListUiState.Error(
            "Función no implementada: No se pueden eliminar usuarios productores desde aquí"
        )

        // TODO: Cuando el backend tenga un endpoint DELETE /api/auth/users/{id}
        // viewModelScope.launch {
        //     val result = repository.deleteUser(id)
        //     if (result.isSuccess) {
        //         loadProductores()
        //     } else {
        //         _uiState.value = ProductoresListUiState.Error(
        //             result.exceptionOrNull()?.message ?: "Error al eliminar"
        //         )
        //     }
        // }
    }
}

/**
 * Estados de UI para la lista de productores (Usuarios con rol PRODUCTOR)
 */
sealed class ProductoresListUiState {
    object Loading : ProductoresListUiState()
    object Empty : ProductoresListUiState()
    data class Success(val productores: List<User>) : ProductoresListUiState()
    data class Error(val message: String) : ProductoresListUiState()
}
