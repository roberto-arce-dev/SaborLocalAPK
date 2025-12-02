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
        ubicacion = ubicacion ?: "",
        telefono = telefono ?: "",
        email = email ?: "",
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

/**
 * Convierte ProductoDto a Producto
 *
 * **IMPORTANTE:** El API puede retornar productor de 3 formas:
 * 1. String (solo ID)
 * 2. Map parcial {_id, telefono}
 * 3. Map completo con todos los campos
 *
 * Este mapper maneja los 3 casos.
 */
fun ProductoDto.toDomain(): Producto {
    val productorObj = when (productor) {
        is String -> {
            // Caso 1: Solo ID
            Productor(
                id = productor,
                nombre = null,
                ubicacion = null,
                telefono = null,
                email = null
            )
        }
        is Map<*, *> -> {
            // Caso 2 y 3: Objeto parcial o completo
            val map = productor as Map<String, Any>
            Productor(
                id = map["_id"] as? String ?: "",
                nombre = map["nombre"] as? String,
                ubicacion = map["ubicacion"] as? String,
                telefono = map["telefono"] as? String,
                email = map["email"] as? String,
                imagen = map["imagen"] as? String,
                imagenThumbnail = map["imagenThumbnail"] as? String
            )
        }
        else -> {
            // Fallback: Productor vacío
            Productor(
                id = "",
                nombre = null,
                ubicacion = null,
                telefono = null,
                email = null
            )
        }
    }

    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        unidad = unidad,
        stock = stock,
        productor = productorObj,
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
 *
 * **ACTUALIZADO:** Alineado con la estructura real del backend
 * El backend retorna: producto (String ID), cantidad, precio
 */
fun PedidoItemDto.toDomain(): PedidoItem {
    // Crear producto mínimo con los datos disponibles
    val productoMinimo = Producto(
        id = producto,  // ✅ Actualizado de productoId a producto
        nombre = "Producto #$producto",  // ✅ No viene del backend, usar ID
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
 * Convierte PedidoDto a Pedido
 *
 * **ACTUALIZADO:** El backend ahora usa .populate('cliente') y retorna objeto completo
 * El backend retorna: cliente (objeto con _id y nombre), items, createdAt
 */
fun PedidoDto.toDomain(): Pedido? {
    // Validar que tengamos fecha
    val fecha = createdAt ?: return null

    // Usar el objeto cliente completo del backend
    val clienteObj = Cliente(
        id = cliente.id,
        nombre = cliente.nombre ?: "Cliente #${cliente.id}",
        email = cliente.email ?: "",
        telefono = cliente.telefono ?: "",
        direccion = cliente.direccion ?: direccionEntrega ?: ""
    )

    // Convertir items
    val itemsConverted = items.map { it.toDomain() }

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
    return map { it.toDomain() }
}

fun List<ClienteDto>.toClienteDomainList(): List<Cliente> {
    return map { it.toDomain() }
}

fun List<PedidoDto>.toPedidoDomainList(): List<Pedido> {
    return mapNotNull { it.toDomain() }  // ✅ Usa mapNotNull para filtrar nulls
}
