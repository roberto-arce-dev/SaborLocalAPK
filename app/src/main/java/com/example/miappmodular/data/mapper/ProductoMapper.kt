package com.example.miappmodular.data.mapper

import com.example.miappmodular.data.remote.dto.producto.ProductoDto
import com.example.miappmodular.data.remote.dto.productor.ProductorDto
import com.example.miappmodular.model.Producto
import com.example.miappmodular.model.Productor

/**
 * Mappers para convertir DTOs de Producto a modelos de dominio
 *
 * **Importante:** El API real solo retorna `productorId` como String,
 * no el objeto completo del productor. Por lo tanto, creamos un Productor
 * mínimo con solo el ID hasta que obtengamos los datos completos.
 *
 * **Flujo recomendado:**
 * 1. Obtener productos con `getProductosByProductor(productorId)`
 * 2. Los productos ya vienen filtrados por ese productor
 * 3. Usar el `productorId` para obtener datos del productor si es necesario
 */

/**
 * Convierte ProductoDto a Producto (modelo de dominio)
 *
 * **Limitación:** Como el API solo retorna `productorId` y no el objeto,
 * creamos un Productor mínimo. En el ViewModel, debes obtener los datos
 * completos del productor si los necesitas.
 */
fun ProductoDto.toModel(): Producto {
    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        unidad = unidad,
        stock = stock,
        productor = Productor(
            id = getProductorId(),
            nombre = null,
            ubicacion = null,
            telefono = null,
            email = null
        ),
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

/**
 * Convierte ProductoDto a Producto con datos completos del productor
 *
 * Usa esta función cuando ya tengas los datos del productor.
 *
 * @param productorData Datos completos del productor
 */
fun ProductoDto.toModelWithProductor(productorData: Productor): Producto {
    return Producto(
        id = id,
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        unidad = unidad,
        stock = stock,
        productor = productorData,
        imagen = imagen,
        imagenThumbnail = imagenThumbnail
    )
}

/**
 * Convierte ProductorDto a Productor (modelo de dominio)
 */
fun ProductorDto.toModel(): Productor {
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
