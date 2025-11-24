package com.example.miappmodular.model

/**
 * Modelos de Dominio para SaborLocal
 *
 * Estos modelos representan las entidades del dominio de negocio.
 * Son independientes de la implementación del API (DTOs) y de la base de datos.
 * Facilitan el testing y permiten cambiar la implementación sin afectar la UI.
 */

/**
 * Modelo de dominio para Productor
 */
data class Productor(
    val id: String,
    val nombre: String,
    val ubicacion: String,
    val telefono: String,
    val email: String,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
) {
    /**
     * Retorna la URL completa de la imagen
     */
    fun getImagenUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagen?.let { "$baseUrl/$it" }
    }

    /**
     * Retorna la URL completa del thumbnail
     */
    fun getThumbnailUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagenThumbnail?.let { "$baseUrl/$it" }
    }
}

/**
 * Modelo de dominio para Producto
 */
data class Producto(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val unidad: String,
    val stock: Int,
    val productor: Productor,  // Siempre contiene el productor completo
    val imagen: String? = null,
    val imagenThumbnail: String? = null
) {
    /**
     * Retorna el precio formateado
     */
    fun getPrecioFormateado(): String {
        return "$${"%.2f".format(precio)} / $unidad"
    }

    /**
     * Indica si hay stock disponible
     */
    fun tieneStock(): Boolean = stock > 0

    /**
     * Retorna la URL completa de la imagen
     */
    fun getImagenUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagen?.let { "$baseUrl/$it" }
    }

    /**
     * Retorna la URL completa del thumbnail
     */
    fun getThumbnailUrl(baseUrl: String = "http://10.0.2.2:3008"): String? {
        return imagenThumbnail?.let { "$baseUrl/$it" }
    }

    /**
     * Indica si el stock es bajo (menos de 10 unidades)
     */
    fun stockBajo(): Boolean = stock in 1..9
}

/**
 * Modelo de dominio para Cliente
 */
data class Cliente(
    val id: String,
    val nombre: String,
    val email: String,
    val telefono: String,
    val direccion: String
)

/**
 * Modelo de dominio para Usuario
 * Representa un usuario autenticado del sistema
 */
data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val role: String,  // CLIENTE, PRODUCTOR, ADMIN
    val telefono: String? = null,
    val ubicacion: String? = null,
    val direccion: String? = null
) {
    /**
     * Verifica si el usuario es CLIENTE
     */
    fun isCliente(): Boolean = role == "CLIENTE"

    /**
     * Verifica si el usuario es PRODUCTOR
     */
    fun isProductor(): Boolean = role == "PRODUCTOR"

    /**
     * Verifica si el usuario es ADMIN
     */
    fun isAdmin(): Boolean = role == "ADMIN"
}

/**
 * Item de producto en un pedido
 */
data class PedidoItem(
    val producto: Producto,
    val cantidad: Int,
    val precio: Double
) {
    /**
     * Calcula el subtotal del item
     */
    fun getSubtotal(): Double = cantidad * precio

    /**
     * Retorna el subtotal formateado
     */
    fun getSubtotalFormateado(): String {
        return "$${"%.2f".format(getSubtotal())}"
    }
}

/**
 * Modelo de dominio para Pedido
 */
data class Pedido(
    val id: String,
    val cliente: Cliente,
    val items: List<PedidoItem>,
    val total: Double,
    val estado: EstadoPedido,
    val fecha: String
) {
    /**
     * Retorna el total formateado
     */
    fun getTotalFormateado(): String {
        return "$${"%.2f".format(total)}"
    }

    /**
     * Calcula la cantidad total de items en el pedido
     */
    fun getCantidadTotal(): Int {
        return items.sumOf { it.cantidad }
    }
}

/**
 * Estados posibles de un pedido
 */
enum class EstadoPedido(val displayName: String) {
    PENDIENTE("Pendiente"),
    EN_PREPARACION("En Preparación"),
    EN_CAMINO("En Camino"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    companion object {
        fun fromString(estado: String): EstadoPedido {
            return when (estado.lowercase()) {
                "pendiente" -> PENDIENTE
                "en_preparacion" -> EN_PREPARACION
                "en_camino", "en camino" -> EN_CAMINO
                "entregado" -> ENTREGADO
                "cancelado" -> CANCELADO
                else -> PENDIENTE
            }
        }
    }
}

/**
 * Modelo de dominio para Entrega
 */
data class Entrega(
    val id: String,
    val pedido: Pedido,
    val direccion: String,
    val fechaEntrega: String,
    val estado: EstadoEntrega,
    val repartidor: String
)

/**
 * Estados posibles de una entrega
 */
enum class EstadoEntrega(val displayName: String) {
    PENDIENTE("Pendiente"),
    EN_CAMINO("En Camino"),
    ENTREGADO("Entregado");

    companion object {
        fun fromString(estado: String): EstadoEntrega {
            return when (estado.lowercase()) {
                "pendiente" -> PENDIENTE
                "en_camino", "en camino" -> EN_CAMINO
                "entregado" -> ENTREGADO
                else -> PENDIENTE
            }
        }
    }
}

/**
 * Resultado de una operación (Success/Error)
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Result<Nothing>()

    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    fun exceptionOrNull(): Throwable? = when (this) {
        is Success -> null
        is Error -> exception
    }
}
