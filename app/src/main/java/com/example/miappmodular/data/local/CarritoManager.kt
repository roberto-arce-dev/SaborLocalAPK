package com.example.miappmodular.data.local

import com.example.miappmodular.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor del carrito de compras (Singleton)
 *
 * **¿Por qué un singleton local?**
 * - El carrito es temporal y no necesita persistirse en base de datos
 * - Se borra al crear el pedido o cerrar la app
 * - Es simple de entender para estudiantes (solo una lista en memoria)
 * - StateFlow permite observar cambios desde cualquier pantalla
 *
 * **Arquitectura simple:**
 * ```
 * ProductosListScreen → [Añadir producto] → CarritoManager.addItem()
 *                                              ↓
 * CarritoScreen ← Observa cambios ← CarritoManager.items (StateFlow)
 *      ↓
 * CheckoutScreen → [Confirmar pedido] → CarritoManager.clear()
 * ```
 *
 * **Ejemplo de uso:**
 * ```kotlin
 * // Añadir producto al carrito
 * CarritoManager.addItem(producto, cantidad = 2)
 *
 * // Observar items del carrito
 * val items by CarritoManager.items.collectAsState()
 *
 * // Calcular total
 * val total = CarritoManager.getTotal()
 *
 * // Limpiar carrito después de crear pedido
 * CarritoManager.clear()
 * ```
 */
object CarritoManager {

    /**
     * Item del carrito con cantidad editable
     */
    data class CarritoItem(
        val producto: Producto,
        val cantidad: Int
    ) {
        /**
         * Calcula el subtotal del item
         */
        fun getSubtotal(): Double = producto.precio * cantidad

        /**
         * Retorna el subtotal formateado
         */
        fun getSubtotalFormateado(): String {
            return "$${"%.2f".format(getSubtotal())}"
        }
    }

    // Lista mutable privada de items
    private val _items = MutableStateFlow<List<CarritoItem>>(emptyList())

    /**
     * StateFlow observable de items del carrito
     * Las pantallas pueden observar este flow para actualizarse automáticamente
     */
    val items: StateFlow<List<CarritoItem>> = _items.asStateFlow()

    /**
     * Añade un producto al carrito
     *
     * Si el producto ya existe, actualiza la cantidad en lugar de duplicarlo.
     *
     * @param producto Producto a añadir
     * @param cantidad Cantidad a añadir (por defecto 1)
     */
    fun addItem(producto: Producto, cantidad: Int = 1) {
        val currentItems = _items.value.toMutableList()

        // Buscar si el producto ya existe en el carrito
        val existingItemIndex = currentItems.indexOfFirst { it.producto.id == producto.id }

        if (existingItemIndex >= 0) {
            // Actualizar cantidad del producto existente
            val existingItem = currentItems[existingItemIndex]
            val newCantidad = existingItem.cantidad + cantidad

            // Verificar que no exceda el stock disponible
            val finalCantidad = if (newCantidad > producto.stock) {
                producto.stock
            } else {
                newCantidad
            }

            currentItems[existingItemIndex] = existingItem.copy(cantidad = finalCantidad)
        } else {
            // Añadir nuevo producto al carrito
            val finalCantidad = if (cantidad > producto.stock) producto.stock else cantidad
            currentItems.add(CarritoItem(producto, finalCantidad))
        }

        _items.value = currentItems
    }

    /**
     * Actualiza la cantidad de un producto en el carrito
     *
     * @param productoId ID del producto
     * @param nuevaCantidad Nueva cantidad (debe ser > 0)
     */
    fun updateCantidad(productoId: String, nuevaCantidad: Int) {
        if (nuevaCantidad <= 0) {
            removeItem(productoId)
            return
        }

        val currentItems = _items.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.producto.id == productoId }

        if (itemIndex >= 0) {
            val item = currentItems[itemIndex]

            // Verificar que no exceda el stock
            val finalCantidad = if (nuevaCantidad > item.producto.stock) {
                item.producto.stock
            } else {
                nuevaCantidad
            }

            currentItems[itemIndex] = item.copy(cantidad = finalCantidad)
            _items.value = currentItems
        }
    }

    /**
     * Elimina un producto del carrito
     *
     * @param productoId ID del producto a eliminar
     */
    fun removeItem(productoId: String) {
        _items.value = _items.value.filter { it.producto.id != productoId }
    }

    /**
     * Incrementa la cantidad de un producto en 1
     *
     * @param productoId ID del producto
     */
    fun incrementCantidad(productoId: String) {
        val currentItems = _items.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.producto.id == productoId }

        if (itemIndex >= 0) {
            val item = currentItems[itemIndex]
            val newCantidad = item.cantidad + 1

            // Verificar que no exceda el stock
            if (newCantidad <= item.producto.stock) {
                currentItems[itemIndex] = item.copy(cantidad = newCantidad)
                _items.value = currentItems
            }
        }
    }

    /**
     * Decrementa la cantidad de un producto en 1
     *
     * Si la cantidad llega a 0, elimina el producto del carrito.
     *
     * @param productoId ID del producto
     */
    fun decrementCantidad(productoId: String) {
        val currentItems = _items.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.producto.id == productoId }

        if (itemIndex >= 0) {
            val item = currentItems[itemIndex]
            val newCantidad = item.cantidad - 1

            if (newCantidad <= 0) {
                currentItems.removeAt(itemIndex)
            } else {
                currentItems[itemIndex] = item.copy(cantidad = newCantidad)
            }

            _items.value = currentItems
        }
    }

    /**
     * Calcula el total del carrito
     *
     * @return Total en formato Double
     */
    fun getTotal(): Double {
        return _items.value.sumOf { it.getSubtotal() }
    }

    /**
     * Retorna el total formateado como String
     *
     * @return Total formateado (ej: "$45.50")
     */
    fun getTotalFormateado(): String {
        return "$${"%.2f".format(getTotal())}"
    }

    /**
     * Retorna la cantidad total de items en el carrito
     *
     * @return Suma de todas las cantidades
     */
    fun getCantidadTotal(): Int {
        return _items.value.sumOf { it.cantidad }
    }

    /**
     * Verifica si el carrito está vacío
     *
     * @return true si no hay items, false en caso contrario
     */
    fun isEmpty(): Boolean {
        return _items.value.isEmpty()
    }

    /**
     * Limpia el carrito completamente
     *
     * Se llama después de crear un pedido exitosamente.
     */
    fun clear() {
        _items.value = emptyList()
    }

    /**
     * Obtiene la cantidad actual de un producto en el carrito
     *
     * @param productoId ID del producto
     * @return Cantidad del producto (0 si no está en el carrito)
     */
    fun getCantidadProducto(productoId: String): Int {
        return _items.value.find { it.producto.id == productoId }?.cantidad ?: 0
    }
}
