package com.example.miappmodular.data.remote.dto.producto

/**
 * Request para actualizar un Producto
 */
data class UpdateProductoRequest(
    val nombre: String? = null,
    val descripcion: String? = null,
    val precio: Double? = null,
    val unidad: String? = null,
    val stock: Int? = null,
    val productor: String? = null,
    val imagen: String? = null,
    val imagenThumbnail: String? = null
)
