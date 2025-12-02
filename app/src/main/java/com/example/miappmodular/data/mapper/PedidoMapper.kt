package com.example.miappmodular.data.mapper

import com.example.miappmodular.data.remote.dto.pedido.PedidoDto
import com.example.miappmodular.data.remote.dto.pedido.PedidoItemDto
import com.example.miappmodular.model.*

/**
 * Mappers para convertir DTOs de Pedido a modelos de dominio
 *
 * **Actualizado según la respuesta REAL del backend SaborLocal:**
 * - El backend retorna `cliente` como objeto completo (usando .populate())
 * - Los items tienen solo `producto` (String ID), `cantidad` y `precio`
 * - Usa `items` en vez de `productos`
 * - Usa `createdAt` como fecha del pedido
 *
 * **¿Por qué usamos Mappers?**
 * - Separan la capa de datos (API) de la capa de dominio (ViewModels/UI)
 * - Facilitan el testing al tener modelos limpios
 * - Permiten cambiar el API sin romper la UI
 */

/**
 * Convierte PedidoItemDto a PedidoItem (modelo de dominio)
 *
 * **Nota:** El backend solo retorna el ID del producto en los items del pedido.
 * Creamos un Producto mínimo solo con ID y precio.
 * Para obtener los datos completos (nombre, imagen, etc.) se necesitaría
 * hacer GET /api/producto/{id} por cada producto.
 */
fun PedidoItemDto.toModel(): PedidoItem {
    // Crear producto mínimo con los datos disponibles (solo ID y precio)
    val productoMinimo = Producto(
        id = producto,  // ✅ Ahora usa "producto" en lugar de "productoId"
        nombre = "Producto #$producto",  // ✅ No tenemos nombre, usar ID como fallback
        descripcion = "",
        precio = precio,
        unidad = "",
        stock = 0,
        productor = Productor(
            id = "",
            nombre = null,
            ubicacion = null,
            telefono = null,
            email = null,
            imagen = null,
            imagenThumbnail = null
        )
    )

    return PedidoItem(
        producto = productoMinimo,
        cantidad = cantidad,
        precio = precio
    )
}

/**
 * Convierte PedidoDto a Pedido (modelo de dominio)
 *
 * **Actualización:** El backend ahora retorna el cliente como objeto completo
 * usando `.populate('cliente')` en MongoDB.
 *
 * @return Pedido con datos disponibles del backend, o null si falta información crítica
 */
fun PedidoDto.toModel(): Pedido? {
    // Validar que tengamos la fecha (createdAt es requerida)
    val fecha = createdAt ?: return null

    // Usar el objeto cliente completo del backend
    val clienteObj = Cliente(
        id = cliente.id,
        nombre = cliente.nombre ?: "Cliente #${cliente.id}",  // Nombre real o fallback
        email = cliente.email ?: "",
        telefono = cliente.telefono ?: "",
        direccion = cliente.direccion ?: direccionEntrega ?: ""
    )

    // Convertir items
    val itemsObj = items.map { it.toModel() }

    // Convertir estado
    val estadoObj = EstadoPedido.fromString(estado)

    return Pedido(
        id = id,
        cliente = clienteObj,
        items = itemsObj,
        total = total,
        estado = estadoObj,
        fecha = fecha
    )
}
