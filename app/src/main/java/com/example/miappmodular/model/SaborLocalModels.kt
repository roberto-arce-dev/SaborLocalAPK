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
 *
 * **IMPORTANTE:** Los campos pueden ser null dependiendo del endpoint:
 * - `/producto` retorna productor parcial (solo _id y telefono)
 * - `/productor-profile` retorna productor completo
 */
data class Productor(
    val id: String,
    val nombre: String? = null,
    val ubicacion: String? = null,
    val telefono: String? = null,
    val email: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
) {
    /**
     * Obtiene el nombre del productor o un valor por defecto
     */
    fun getDisplayName(): String = nombre ?: "Productor"
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
 *
 * **IMPORTANTE:** El campo `nombre` puede ser null.
 * Algunos usuarios en el sistema no tienen nombre configurado.
 */
data class User(
    val id: String,
    val nombre: String? = null,  // ✅ Nullable - algunos usuarios no lo tienen
    val email: String,
    val role: String,  // CLIENTE, PRODUCTOR, ADMIN
    val telefono: String? = null,
    val ubicacion: String? = null,
    val direccion: String? = null
) {
    /**
     * Obtiene el nombre del usuario o un valor por defecto
     */
    fun getDisplayName(): String = nombre ?: email.substringBefore("@")

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
 * Resultado de una operación de API o repositorio (Success/Error)
 *
 * Renombrado de `Result` a `ApiResult` para evitar colisión de nombres con
 * `kotlin.Result` de la biblioteca estándar de Kotlin.
 *
 * Esto previene:
 * - Ambigüedad en compilación que requiere imports explícitos
 * - Confusión al leer código (¿cuál Result es este?)
 * - Bugs potenciales al usar el tipo incorrecto de Result
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : ApiResult<Nothing>()

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
