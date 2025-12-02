package com.example.miappmodular.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.miappmodular.data.local.CarritoManager
import kotlinx.coroutines.flow.StateFlow
/**
 * ViewModel para el carrito de compras
 *
 * **Arquitectura simple:**
 * Este ViewModel es un wrapper ligero sobre CarritoManager.
 * La lógica real está en CarritoManager (singleton) para facilitar
 * el acceso desde múltiples pantallas.
 * **Flujo:**
 * - ProductorDetalleScreen → Añade productos al carrito
 * - CarritoScreen → Muestra y gestiona el carrito
 * - CheckoutScreen → Crea el pedido y limpia el carrito
 * **Ejemplo de uso:**
 * ```kotlin
 * val viewModel: CarritoViewModel = viewModel()
 * val items by viewModel.items.collectAsState()
 * // Mostrar total
 * Text("Total: ${viewModel.getTotal()}")
 * // Eliminar item
 * viewModel.removeItem(productoId)
 * ```
 */
class CarritoViewModel(application: Application) : AndroidViewModel(application) {
    /**
    /**
     * StateFlow observable de items del carrito
     */
     */
    val items: StateFlow<List<CarritoManager.CarritoItem>> = CarritoManager.items

    /**
    /**
     * Actualiza la cantidad de un producto en el carrito
     */
     *
    /**
     * @param productoId ID del producto
     * @param nuevaCantidad Nueva cantidad (si es 0, elimina el item)
     */
     */
    fun updateCantidad(productoId: String, nuevaCantidad: Int) {
        CarritoManager.updateCantidad(productoId, nuevaCantidad)
    }

    /**
    /**
     * Incrementa la cantidad de un producto en 1
     */
     */
    fun incrementCantidad(productoId: String) {
        CarritoManager.incrementCantidad(productoId)
    }

    /**
    /**
     * Decrementa la cantidad de un producto en 1
     * Si llega a 0, elimina el producto del carrito
     */
     */
    fun decrementCantidad(productoId: String) {
        CarritoManager.decrementCantidad(productoId)
    }

    /**
    /**
     * Elimina un producto del carrito
     * @param productoId ID del producto a eliminar
     */
     */
    fun removeItem(productoId: String) {
        CarritoManager.removeItem(productoId)
    }

    /**
    /**
     * Calcula el total del carrito
     * @return Total en formato Double
     */
     */
    fun getTotal(): Double {
        return CarritoManager.getTotal()
    }

    /**
    /**
     * Retorna el total formateado
     * @return Total formateado (ej: "$45.50")
     */
     */
    fun getTotalFormateado(): String {
        return CarritoManager.getTotalFormateado()
    }

    /**
    /**
     * Verifica si el carrito está vacío
     * @return true si no hay items, false en caso contrario
     */
     */
    fun isEmpty(): Boolean {
        return CarritoManager.isEmpty()
    }

    /**
    /**
     * Limpia el carrito completamente
     * **Nota:** Solo se debe llamar después de crear el pedido exitosamente.
     */
     */
    fun clear() {
        CarritoManager.clear()
    }

    /**
    /**
     * Obtiene la cantidad total de items
     * @return Suma de todas las cantidades
     */
     */
    fun getCantidadTotal(): Int {
        return CarritoManager.getCantidadTotal()
    }
}
