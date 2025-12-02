package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.miappmodular.data.local.CarritoManager
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.pedido.CreatePedidoRequest
import com.example.miappmodular.data.remote.dto.pedido.PedidoItemRequest
import com.example.miappmodular.model.ApiResult
import com.example.miappmodular.model.Pedido
import com.example.miappmodular.repository.PedidoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para el proceso de checkout (confirmación de pedido)
 *
 * **Flujo del checkout:**
 * 1. Cliente revisa el carrito
 * 2. Confirma el pedido
 * 3. ViewModel crea el pedido en el backend
 * 4. Si es exitoso, limpia el carrito y navega a "pedido exitoso"
 * 5. Si falla, muestra error y mantiene el carrito
 *
 * **Arquitectura simple:**
 * - PedidoRepository para crear el pedido
 * - CarritoManager para obtener items y limpiar después
 * - TokenManager para obtener el ID del usuario actual
 */
class CheckoutViewModel(application: Application) : AndroidViewModel(application) {

    private val pedidoRepository = PedidoRepository()
    private val tokenManager = RetrofitClient.getTokenManager()

    // Estado de la UI
    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Idle)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    // Items del carrito
    val items = CarritoManager.items

    /**
     * Obtiene el total del carrito
     */
    fun getTotal(): Double {
        return CarritoManager.getTotal()
    }

    /**
     * Obtiene el total formateado
     */
    fun getTotalFormateado(): String {
        return CarritoManager.getTotalFormateado()
    }

    /**
     * Obtiene la cantidad total de items
     */
    fun getCantidadTotal(): Int {
        return CarritoManager.getCantidadTotal()
    }

    /**
     * Crea el pedido
     *
     * **Proceso actualizado (alineado con backend):**
     * 1. Valida que el usuario esté autenticado (el backend obtiene el ID del token JWT)
     * 2. Convierte los items del carrito a PedidoItemRequest (solo producto y cantidad)
     * 3. Crea el CreatePedidoRequest con direccionEntrega y notasEntrega opcionales
     * 4. El backend calcula el total y obtiene precios desde la BD (más seguro)
     * 5. Si es exitoso, limpia el carrito
     * 6. Retorna el pedido creado en Success o error en Error
     *
     * @param direccionEntrega Dirección de entrega opcional
     * @param notasEntrega Notas para la entrega opcional
     */
    fun crearPedido(
        direccionEntrega: String? = null,
        notasEntrega: String? = null
    ) {
        // Validar que el carrito no esté vacío
        if (CarritoManager.isEmpty()) {
            _uiState.value = CheckoutUiState.Error("El carrito está vacío")
            return
        }

        // El backend valida el token JWT automáticamente mediante AuthInterceptor
        // Si el token no es válido o ha expirado, el backend retornará 401 Unauthorized
        // No necesitamos validar getCurrentUser() aquí

        _uiState.value = CheckoutUiState.Loading

        viewModelScope.launch {
            // Convertir items del carrito a PedidoItemRequest
            // Solo enviamos producto y cantidad - el backend obtiene el precio de la BD
            val itemsRequest = CarritoManager.items.value.map { carritoItem ->
                PedidoItemRequest(
                    producto = carritoItem.producto.id,
                    cantidad = carritoItem.cantidad
                )
            }

            // Crear el request
            // No enviamos cliente ni total - el backend los maneja
            val request = CreatePedidoRequest(
                items = itemsRequest,
                direccionEntrega = direccionEntrega,
                notasEntrega = notasEntrega
            )

            // Llamar al repository
            when (val result = pedidoRepository.createPedido(request)) {
                is ApiResult.Success -> {
                    // Guardar el pedido para la pantalla de éxito
                    LastPedidoHolder.setLastPedido(result.data)

                    // Limpiar el carrito
                    CarritoManager.clear()

                    // Notificar éxito
                    _uiState.value = CheckoutUiState.Success(result.data)
                }
                is ApiResult.Error -> {
                    _uiState.value = CheckoutUiState.Error(
                        result.message
                    )
                }
            }
        }
    }

    /**
     * Resetea el estado a Idle
     */
    fun resetState() {
        _uiState.value = CheckoutUiState.Idle
    }
}

/**
 * Estados posibles del checkout
 */
sealed class CheckoutUiState {
    object Idle : CheckoutUiState()
    object Loading : CheckoutUiState()
    data class Success(val pedido: Pedido) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
}

/**
 * Objeto companion para almacenar temporalmente el último pedido creado
 * Esto permite pasarlo a la pantalla de éxito sin usar navegación con args complejos
 */
object LastPedidoHolder {
    var lastPedido: Pedido? = null
        private set

    fun setLastPedido(pedido: Pedido) {
        lastPedido = pedido
    }

    fun clearLastPedido() {
        lastPedido = null
    }
}
