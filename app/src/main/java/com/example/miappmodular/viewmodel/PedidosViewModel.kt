package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.model.Pedido
import com.example.miappmodular.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
/**
 * ViewModel para mostrar la lista de pedidos del cliente
 *
 * **Flujo:**
 * 1. Obtiene todos los pedidos del usuario autenticado
 * 2. Los muestra ordenados por fecha (más recientes primero)
 * 3. Permite navegar al detalle de cada pedido
 * **Arquitectura simple:**
 * El backend filtra automáticamente los pedidos por usuario según el token JWT.
 * Un CLIENTE solo ve sus propios pedidos.
 */
class PedidosViewModel(application: Application) : AndroidViewModel(application) {
    private val pedidoRepository = PedidoRepository()
    // Estado de la UI
    private val _uiState = MutableStateFlow<PedidosUiState>(PedidosUiState.Loading)
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()
    init {
        loadPedidos()
    }

    /**
     * Carga la lista de pedidos del usuario
     */
    fun loadPedidos() {
        _uiState.value = PedidosUiState.Loading
        viewModelScope.launch {
            when (val result = pedidoRepository.getAllPedidos()) {
                is ApiResult.Success -> {
                    _uiState.value = if (result.data.isEmpty()) {
                        PedidosUiState.Empty
                    } else {
                        // Ordenar por fecha (más recientes primero)
                        val pedidosOrdenados = result.data.sortedByDescending { it.fecha }
                        PedidosUiState.Success(pedidosOrdenados)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.value = PedidosUiState.Error(
                        result.message
                    )
                }
            }
        }
    }
}

/**
 * Estados posibles de la UI
 */
sealed class PedidosUiState {
    object Loading : PedidosUiState()
    object Empty : PedidosUiState()
    data class Success(val pedidos: List<Pedido>) : PedidosUiState()
    data class Error(val message: String) : PedidosUiState()
}
