package com.example.miappmodular.data.mapper

import com.example.miappmodular.data.remote.dto.productor.ProductorDto
import com.example.miappmodular.data.remote.dto.producto.ProductoDto
import com.example.miappmodular.data.remote.dto.cliente.ClienteDto
import com.example.miappmodular.data.remote.dto.pedido.PedidoDto
import com.example.miappmodular.data.remote.dto.pedido.PedidoItemDto
import com.example.miappmodular.data.remote.dto.entrega.EntregaDto
import com.example.miappmodular.model.*

/**
 * Mappers para convertir entre DTOs (del API) y Modelos de Dominio
 *
 * Los mappers nos permiten:
 * 1. Desacoplar la UI del formato del API
 * 2. Transformar datos (ej: parsear fechas, formatear textos)
 * 3. Manejar casos especiales (campos nulos, valores por defecto)
 */

/**
 * Convierte ProductorDto a Productor
 */
fun ProductorDto.toDomain(): Productor {
    return Productor(
        id = id,
        nombre = nombre,
        ubicacion = ubicacion,
        telefono = telefono,
        email = email,
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

/**
 * Convierte ProductoDto a Producto
 * Requiere que el productor esté populated en el DTO
 */
fun ProductoDto.toDomain(): Producto? {
    // Obtener el productor populated
    val productorPopulated = getProductorPopulated() ?: return null

    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        unidad = unidad,
        stock = stock,
        productor = productorPopulated.toDomain(),
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

/**
 * Convierte ClienteDto a Cliente
 */
fun ClienteDto.toDomain(): Cliente {
    return Cliente(
        id = id,
        nombre = nombre,
        email = email,
        telefono = telefono,
        direccion = direccion
    )
}

/**
 * Convierte PedidoItemDto a PedidoItem
 */
fun PedidoItemDto.toDomain(): PedidoItem? {
    val productoPopulated = getProductoPopulated()?.toDomain() ?: return null

    return PedidoItem(
        producto = productoPopulated,
        cantidad = cantidad,
        precio = precio
    )
}

/**
 * Convierte PedidoDto a Pedido
 */
fun PedidoDto.toDomain(): Pedido? {
    // Convertir cliente
    val clienteObj = when (cliente) {
        is Map<*, *> -> {
            val map = cliente as Map<String, Any>
            ClienteDto(
                id = map["_id"] as? String ?: "",
                nombre = map["nombre"] as? String ?: "",
                email = map["email"] as? String ?: "",
                telefono = map["telefono"] as? String ?: "",
                direccion = map["direccion"] as? String ?: ""
            ).toDomain()
        }
        else -> return null
    }

    // Convertir items
    val itemsConverted = items.mapNotNull { it.toDomain() }
    if (itemsConverted.size != items.size) {
        // Algunos items no se pudieron convertir
        return null
    }

    return Pedido(
        id = id,
        cliente = clienteObj,
        items = itemsConverted,
        total = total,
        estado = EstadoPedido.fromString(estado),
        fecha = fecha
    )
}

/**
 * Convierte EntregaDto a Entrega
 */
fun EntregaDto.toDomain(): Entrega? {
    // Convertir pedido
    val pedidoObj = when (pedido) {
        is Map<*, *> -> {
            // Aquí necesitaríamos construir un PedidoDto desde el map
            // Por simplicidad, retornamos null si no está populated correctamente
            return null
        }
        else -> return null
    }
}

/**
 * Extension functions para listas
 * Nota: Se usan nombres específicos para evitar conflictos de firma JVM (type erasure)
 */
fun List<ProductorDto>.toProductorDomainList(): List<Productor> {
    return map { it.toDomain() }
}

fun List<ProductoDto>.toProductoDomainList(): List<Producto> {
    return mapNotNull { it.toDomain() }
}

fun List<ClienteDto>.toClienteDomainList(): List<Cliente> {
    return map { it.toDomain() }
}

fun List<PedidoDto>.toPedidoDomainList(): List<Pedido> {
    return mapNotNull { it.toDomain() }
}
