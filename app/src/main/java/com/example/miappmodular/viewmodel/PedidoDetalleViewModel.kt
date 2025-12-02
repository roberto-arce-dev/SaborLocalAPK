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
 * ViewModel para mostrar el detalle de un pedido específico
 *
 * **Flujo:**
 * 1. Obtiene el pedido por ID
 * 2. Muestra información detallada:
 *    - Items con cantidades y precios
 *    - Total del pedido
 *    - Estado actual (pendiente, en camino, entregado)
 *    - Fecha del pedido
 * **Arquitectura simple:**
 * - PedidoRepository para obtener el pedido
 */
class PedidoDetalleViewModel(application: Application) : AndroidViewModel(application) {
    private val pedidoRepository = PedidoRepository()
    // Estado de la UI
    private val _uiState = MutableStateFlow<PedidoDetalleUiState>(PedidoDetalleUiState.Loading)
    val uiState: StateFlow<PedidoDetalleUiState> = _uiState.asStateFlow()

    /**
     * Carga el detalle de un pedido
     *
     * @param pedidoId ID del pedido
     */
    fun loadPedido(pedidoId: String) {
        _uiState.value = PedidoDetalleUiState.Loading
        viewModelScope.launch {
            when (val result = pedidoRepository.getPedido(pedidoId)) {
                is ApiResult.Success -> {
                    _uiState.value = PedidoDetalleUiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = PedidoDetalleUiState.Error(
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
sealed class PedidoDetalleUiState {
    object Loading : PedidoDetalleUiState()
    data class Success(val pedido: Pedido) : PedidoDetalleUiState()
    data class Error(val message: String) : PedidoDetalleUiState()
}
