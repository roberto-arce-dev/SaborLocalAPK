package com.example.miappmodular.repository

import com.example.miappmodular.data.mapper.toModel
import com.example.miappmodular.data.remote.RetrofitClient
import com.example.miappmodular.data.remote.dto.pedido.CreatePedidoRequest
import com.example.miappmodular.model.Pedido
import com.example.miappmodular.model.ApiResult

/**
 * Repository para gestionar pedidos
 *
 * **Arquitectura simple para estudiantes:**
 * - Usa RetrofitClient singleton directamente (sin DI)
 * - Convierte DTOs a modelos de dominio usando mappers
 * - Maneja errores de red y devuelve Result<T>
 *
 * **Operaciones disponibles:**
 * - `getAllPedidos()` - Obtiene todos los pedidos del usuario
 * - `getPedido(id)` - Obtiene un pedido específico
 * - `createPedido()` - Crea un nuevo pedido
 * - `updateEstadoPedido()` - Actualiza el estado de un pedido
 * - `deletePedido()` - Elimina un pedido
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * val repository = PedidoRepository()
 * val result = repository.getAllPedidos()
 *
 * result.onSuccess { pedidos ->
 *     // Mostrar lista de pedidos
 * }.onFailure { error ->
 *     // Mostrar error
 * }
 * ```
 */
class PedidoRepository {

    private val apiService = RetrofitClient.saborLocalPedidoApiService

    /**
     * Obtiene todos los pedidos
     *
     * El backend automáticamente filtra por usuario según el token JWT.
     * Un CLIENTE solo ve sus propios pedidos.
     * Un ADMIN ve todos los pedidos del sistema.
     *
     * @return Result con lista de pedidos o error
     */
    suspend fun getAllPedidos(): ApiResult<List<Pedido>> {
        return try {
            val response = apiService.getPedidos()

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    // Convertir lista de DTOs a modelos de dominio, filtrando nulls
                    val pedidos = apiResponse.data.mapNotNull { it.toModel() }
                    ApiResult.Success(pedidos)
                } else {
                    ApiResult.Error(apiResponse?.message ?: "Error al obtener pedidos")
                }
            } else {
                ApiResult.Error("Error en el servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error desconocido", e)
        }
    }

    /**
     * Obtiene el historial de pedidos de un cliente específico
     *
     * **Endpoint clave para EP3:** GET /api/pedido/cliente/{clienteId}
     * Este endpoint retorna todos los pedidos realizados por un cliente.
     *
     * @param clienteId ID del cliente
     * @return Result con lista de pedidos del cliente o error
     */
    suspend fun getPedidosByCliente(clienteId: String): ApiResult<List<Pedido>> {
        return try {
            val response = apiService.getPedidosByCliente(clienteId)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    // Convertir lista de DTOs a modelos de dominio, filtrando nulls
                    val pedidos = apiResponse.data.mapNotNull { it.toModel() }
                    ApiResult.Success(pedidos)
                } else {
                    ApiResult.Error(apiResponse?.message ?: "Error al obtener pedidos del cliente")
                }
            } else {
                ApiResult.Error("Error en el servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error desconocido", e)
        }
    }

    /**
     * Obtiene un pedido específico por ID
     *
     * @param id ID del pedido
     * @return Result con el pedido o error
     */
    suspend fun getPedido(id: String): ApiResult<Pedido> {
        return try {
            val response = apiService.getPedido(id)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    val pedido = apiResponse.data.toModel()

                    if (pedido != null) {
                        ApiResult.Success(pedido)
                    } else {
                        ApiResult.Error("Error al convertir el pedido: datos incompletos del backend")
                    }
                } else {
                    ApiResult.Error(apiResponse?.message ?: "Error al obtener el pedido")
                }
            } else {
                ApiResult.Error("Error en el servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error desconocido", e)
        }
    }

    /**
     * Crea un nuevo pedido
     *
     * **Flujo de creación:**
     * 1. El usuario arma su carrito (gestión local)
     * 2. Al hacer checkout, se llama a esta función
     * 3. El backend crea el pedido y lo asocia al usuario autenticado
     * 4. Retorna el pedido creado con su ID y estado "pendiente"
     *
     * @param request Request con items y total del pedido
     * @return Result con el pedido creado o error
     */
    suspend fun createPedido(request: CreatePedidoRequest): ApiResult<Pedido> {
        return try {
            val response = apiService.createPedido(request)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    val pedido = apiResponse.data.toModel()

                    if (pedido != null) {
                        ApiResult.Success(pedido)
                    } else {
                        ApiResult.Error("Error al convertir el pedido: datos incompletos del backend")
                    }
                } else {
                    ApiResult.Error(apiResponse?.message ?: "Error al crear el pedido")
                }
            } else {
                ApiResult.Error("Error en el servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error desconocido", e)
        }
    }

    /**
     * Actualiza el estado de un pedido
     *
     * **Estados posibles:**
     * - "pendiente" - Pedido recién creado
     * - "en_preparacion" - El productor está preparando el pedido
     * - "en_camino" - El pedido está siendo entregado
     * - "entregado" - Pedido entregado exitosamente
     * - "cancelado" - Pedido cancelado
     *
     * @param id ID del pedido
     * @param nuevoEstado Nuevo estado del pedido
     * @return Result con el pedido actualizado o error
     */
    suspend fun updateEstadoPedido(id: String, nuevoEstado: String): ApiResult<Pedido> {
        return try {
            val request = mapOf("estado" to nuevoEstado)
            val response = apiService.updatePedido(id, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true && apiResponse.data != null) {
                    val pedido = apiResponse.data.toModel()

                    if (pedido != null) {
                        ApiResult.Success(pedido)
                    } else {
                        ApiResult.Error("Error al convertir el pedido: datos incompletos del backend")
                    }
                } else {
                    ApiResult.Error(apiResponse?.message ?: "Error al actualizar el pedido")
                }
            } else {
                ApiResult.Error("Error en el servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error desconocido", e)
        }
    }

    /**
     * Elimina un pedido
     *
     * **Nota:** Solo el usuario propietario o un ADMIN pueden eliminar un pedido.
     * Generalmente solo se permite eliminar pedidos en estado "pendiente".
     *
     * @param id ID del pedido
     * @return Result success o error
     */
    suspend fun deletePedido(id: String): ApiResult<Unit> {
        return try {
            val response = apiService.deletePedido(id)

            if (response.isSuccessful) {
                val apiResponse = response.body()

                if (apiResponse?.success == true) {
                    ApiResult.Success(Unit)
                } else {
                    ApiResult.Error(apiResponse?.message ?: "Error al eliminar el pedido")
                }
            } else {
                ApiResult.Error("Error en el servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Error desconocido", e)
        }
    }
}
